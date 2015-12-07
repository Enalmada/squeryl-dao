# squeryl-dao [![Build Status](https://travis-ci.org/Enalmada/squeryl-dao.svg?branch=master)](https://travis-ci.org/Enalmada/squeryl-dao) [![Join the chat at https://gitter.im/Enalmada/squeryl-dao](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Enalmada/squeryl-dao?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Squeryl is an amazing scala ORM.  This module attempts to make starting with squeryl more convenient so you can focus on your business logic.

#### Version information
**I believe squeryl-dao needs Play! Framework 2.4.x or later**

squeryl-dao is built and tested with Scala 2.11.7 (from `0.1.0`)

* `2.4.0` to `2.4.x` (last: `0.1.0` - [master branch](https://github.com/enalmada/squeryl-dao/tree/master))

Releases are on [mvnrepository](http://mvnrepository.com/artifact/com.github.enalmada) and snapshots can be found on [sonatype](https://oss.sonatype.org/content/repositories/snapshots/com/github/enalmada).

## Quickstart
Clone the project and go to `samples`. Create a sample database "createdb -U postgres squeryl-dao".  Edit your application.conf with your db settings and run `activator run` to see a sample application.

### Including the Dependencies

```xml
<dependency>
    <groupId>com.github.enalmada</groupId>
    <artifactId>squeryl-dao_2.11</artifactId>
    <version>0.1.0</version>
</dependency>
```
or

```scala
val appDependencies = Seq(
  "com.github.enalmada" %% "squeryl-dao" % "0.1.0"
)
```

## Dao Features
* Sample model class "User" extending the dao
* crud controller for model including auditing and optimistic locking
* views showing pagination, basic sorting

## Extras
* bootstrap abstraction with play-bootstrap3
* bootstrap theme with bootswatch
* DRY (dont repeat yourself) flash message handling function and create/edit forms core
* If you need auth, I highly recommend starting with play2-auth.


## Versions
* **TRUNK** [not released in the repository, yet]
  * Getting feedback from experts before making it public.
  * Run "activator publishLocal" for now.

  
## License

Copyright (c) 2015 Adam Lane

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
  
