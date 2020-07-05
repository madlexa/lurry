lurry 0.0.1-SNAPSHOT
====================

GQuery Processor
----------------

|  |teamcity_ci| |travis_ci|
|  |logo|

Lurry is a library with support for custom SQL, which allows using Groovy templates - `GString`. It enables to use groovy constructions in SQL queries. 

Lurry can use following formats for saving queries: `yaml`, `xml` and `json`. 

`files`, `http` and `smb` can be used for data storage. In the future the list of formats and resources can be expanded. 

Lurry Goals: 
------------
* A simple and convenient way to use SQL with object-oriented applications 
* Reducing the number of requests

TODO

DSL
---

.. code:: lurry

    import package.Person
    import package.Contact
    import package.ContactType

    main(person_id): new Person(#person_id, #person_name, contacts, #income - #expense)
    contacts(person_id, contact_id): new Contact(#contact_id, ContactType.of(#contact_type), #contact_value)

 
License
-------
This project is licensed under [Apache License, version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

Pull requests are welcome.

.. |teamcity_ci| image:: https://trifle.beta.teamcity.com/app/rest/builds/buildType:(id:Lurry_Test)/statusIcon
   :target: https://trifle.beta.teamcity.com/viewType.html?buildTypeId=Lurry_Test&guest=1
   :alt: TeamCity build
.. |travis_ci| image:: https://travis-ci.org/madlexa/lurry.png?branch=master
   :target: https://travis-ci.org/madlexa/lurry
   :alt: travis build
.. |logo| image:: https://raw.githubusercontent.com/madlexa/lurry/master/image/lurry.svg
   :target: https://github.com/madlexa/lurry
   :alt: lurry

