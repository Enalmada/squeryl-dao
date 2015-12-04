package test.support

import controllers.Assets
import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import play.api.ApplicationLoader.Context
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.inject.{BindingKey, Injector, SimpleInjector}
import play.api.routing.Router
import play.api.test.{Helpers, WithApplicationLoader}
import router.Routes

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/**
 * This allows to unit test a controller with an actual router, while mocking all
 * dependencies on all the other controllers which are irrelevant to the test.
 */
abstract class WithMockControllers extends Around with Scope {
  private lazy val withLoader = new WithApplicationLoader(
    new MockApplicationLoader(underTest, otherComponents)) {}
  implicit lazy val app = withLoader.app

  /**
   * The concrete controller instance under test.
   */
  def underTest: Any

  /**
   * Can be overridden to provide any additional components to the injector that should
   * be used instead of mocks.
   */
  def otherComponents: Map[Class[_], _] = Map()

  def around[T: AsResult](t: => T): Result = {
    Helpers.running(app)(AsResult.effectively(t))
  }
}

class MockApplicationLoader(controllerUnderTest: Any, otherComponents: Map[Class[_], _])
  extends ApplicationLoader {
  override def load(context: Context): Application = {
    Logger.configure(context.environment)
    val components = new BuiltInComponentsFromContext(context) with MockComponents {
      override def realComponents = otherComponents + (controllerUnderTest.getClass -> controllerUnderTest)
    }
    components.application
  }
}

trait MockComponents extends BuiltInComponents {
  def realComponents: Map[Class[_], _]

  lazy val assets: Assets = injector.instanceOf[Assets]

  override lazy val injector: Injector = new SimpleInjector(new MockInjector, realComponents) + crypto + httpConfiguration

  lazy val router: Router = {
    val m = runtimeMirror(getClass.getClassLoader)
    val constructorWithoutPrefix = typeOf[Routes].decl(termNames.CONSTRUCTOR).alternatives(1).asMethod
    val paramsList = constructorWithoutPrefix.paramLists.head
    val routesParamTypes = paramsList.map(_.typeSignature).map(m.runtimeClass).tail
    val routesArgs = routesParamTypes.map(injector.instanceOf(_))

    val constructorWithPrefix = typeOf[Routes].decl(termNames.CONSTRUCTOR).alternatives.head.asMethod
    val cm = m.reflectClass(typeOf[Routes].typeSymbol.asClass)
    val constructorFunction = cm.reflectConstructor(constructorWithPrefix)
    val routes = constructorFunction(DefaultHttpErrorHandler :: routesArgs ::: List("/contextOfApplication"): _*)
    routes.asInstanceOf[Router]
  }

}

class MockInjector extends Injector {
  def instanceOf[T](implicit ct: ClassTag[T]) = org.mockito.Mockito.mock(implicitly[ClassTag[T]].runtimeClass).asInstanceOf[T]

  def instanceOf[T](clazz: Class[T]) = org.mockito.Mockito.mock(clazz)

  def instanceOf[T](key: BindingKey[T]) = instanceOf(key.clazz)
}