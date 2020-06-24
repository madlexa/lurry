# lurry 0.0.1-SNAPSHOT
GQuery Processor

[![Build Status](https://travis-ci.org/madlexa/lurry.png?branch=master)](https://travis-ci.org/madlexa/lurry)
[![Coverage Status](https://coveralls.io/repos/github/madlexa/lurry/badge.svg?branch=master)](https://coveralls.io/github/madlexa/lurry?branch=master)

![logo](https://rawgit.com/madlexa/lurry/master/image/lurry.svg)

Lurry is a library with support for custom SQL, which allows using Groovy templates - `GString`. It enables to use groovy constructions in SQL queries. 

Lurry can use following formats for saving queries: `yaml`, `xml` and `json`. 

`files`, `http` and `smb` can be used for data storage. In the future the list of formats and resources can be expanded. 

Lurry Goals: 
* A simple and convenient way to use SQL with object-oriented applications 
* Reducing the number of requests

TODO

DSL
```lurry
import package.Person
import package.Contact
import package.ContactType

main(person_id): new Person(#person_id, #person_name, contacts, #income - #expense)
contacts(person_id, contact_id): new Contact(#contact_id, ContactType.of(#contact_type), #contact_value)
```
 
### License
 
This project is licensed under [Apache License, version 2.0](https://www.apache.org/licenses/LICENSE-2.0)


Pull requests are welcome.