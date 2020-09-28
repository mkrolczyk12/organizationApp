# OrganizationApp
Back end part of organizationApp
> Track your expenses, monitor your earnings, be aware of your finances!

## Table of contents
* [General info](#general-info)
* [Technologies and dependencies](#technologies-and-dependencies)
* [Extended info](#extended-info)
* [API endpoints](#API-endpoints)
* [Build instruction](#build-instruction)
* [Features](#features)
* [Status](#status)
* [Contact](#contact)

## General info
The main goal of the application is to track all your daily expenses. The collected data will be a tool for the next functionality - creating statistics, for example showing in which month the user spends the most money. The application gives you the opportunity to create a year, month, category for each of your expenses, making sure from the beginning that each expenditure is in the appropriate section. To use the application, you need to create an account and log in. 

## Technologies and dependencies
* Java - version 11
* Spring 5
* Spring boot - version 2.3.0.RELEASE
* Maven
* SQL - H2 (local profile), MySQL (production profile)
* Hibernate - version 6.1.5.Final
* Flywaydb
* Keycloak open source Identity and Access Management - version 10.0.1
* spring-boot-starter: actuator, data-jpa, hateoas, web, security, starter-test

## Extended info
Communication with the application takes place via JSON API, whose endpoints can be found below. The project structure is expanded according to individual application components. The application has two profiles: local and production. Project also has a configuration that allow/deny uses spring aspects for measures the time of performing each function from the expensesProcess Package (in the future the measurement will cover all functions from each package).
A brief description of each project package:
* aspect - contains all aspects around functions in the application
* categoryExpenses - part of the application related with categories
* expensesProcess - part of the application related with single process (expenditure)
* monthExpenses - part of the application related with months
* yearExpenses - part of the application related with years
* globalControllerAdvice - section related with exception handling at the entire application level
* security - a package responsible for configuration with Keycloak - open source software for identity and Access Management
* mvcConfiguration - section related with HttpServletRequest filters, interceptors

If you want to see the frontend part of application, please follow the link below: <br />
https://github.com/mkrolczyk12/organizationAppFE

## API endpoints
### Year section
#### Add year (body attributes: year,description) <br />
```
POST /moneyapp/years
```
Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Created  |
| incorrect validation | Bad request  |

Consumes: <br />
* application/json
#### Add month to chosen year (body attributes: month, description) <br />
```
POST /moneyapp/years/{yearId}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | yearId  | year id (not number!)  | Short  |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Created  |
| incorrect validation | Bad request  |

Consumes: <br />
* application/json
#### Read all years  <br />
```
GET /moneyapp/years
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | sort  | sort by chosen attribute  | Pageable  |
| Path  | size  | select size  | Pageable  |
| Path  | page  | select page  | Pageable  | 

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request  |

Produces: <br />
* application/json
#### Read all years + their months <br />
```
GET /moneyapp/years?months
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | months | word 'months' | String |
| Path  | sort  | sort by chosen attribute  | Pageable  |
| Path  | size  | select size  | Pageable  |
| Path  | page  | select page  | Pageable  |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request  |

Produces: <br />
* application/json
#### Read one year content
```
GET /moneyapp/years/{yearId}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | yearId | year id (not number!) | Short |
| Path  | sort  | sort by chosen attribute  | Pageable  |
| Path  | size  | select size  | Pageable  |
| Path  | page  | select page  | Pageable  |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Produces: <br />
* application/json
#### Full year update
```
PUT /moneyapp/years/{yearId}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | yearId | year id (not number!) | Short |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | NoContent  |
| incorrect validation | Bad request  |

Consumes: <br />
* application/json
#### Part year update
```
PATCH /moneyapp/years/{yearId}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | yearId | year id (not number!) | Short |

Consumes: <br />
* application/json
#### Delete year (only if year content is empty)
```
DELETE /moneyapp/years/{yearId}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | yearId | year id (not number!) | Short |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | NoContent  |
| incorrect validation | Bad request  |

Consumes: <br />
* application/json
### Month section
#### Add month in given year (body attributes: month, description) <br />
```
POST /moneyapp/months?year={yearNumber}
```
Parameters <br />
| Type  | Name | Description | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | year  | year number (not id!) | String  |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Created |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Add month with categories in given year (body attributes: month, description, list of categories (type, description)) <br />
```
POST /moneyapp/months?year={yearNumber}&categories
```
Parameters <br />
| Type  | Name | Description | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | year  | year number (not id!) | String  |
| Path | categories | word 'categories' | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Created  |
| incorrect validation | Bad request  |

Consumes: <br />
* application/json
#### Add category to chosen month in given year (body attributes: type, description) <br />
```
POST /moneyapp/months/{monthId}?year={yearNumber}
```
Parameters <br />
| Type  | Name | Description | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | monthId  | month id (not number!) | Integer  |
| Path  | year  | year number (not id!) | String  |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Created |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Read all months in given year  <br />
```
GET /moneyapp/months?year={yearNumber}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | year  | year number (not id!) | String |
| Path  | sort  | sort by chosen attribute  | Pageable  |
| Path  | size  | select size  | Pageable  |
| Path  | page  | select page  | Pageable  | 

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Produces: <br />
* application/json
#### Read all months + their categories in given year <br />
```
GET /moneyapp/months?year={yearNumber}&categories
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | year  | year number (not id!) | String |
| Path | categories | word 'categories' | String |
| Path  | sort  | sort by chosen attribute  | Pageable  |
| Path  | size  | select size  | Pageable  |
| Path  | page  | select page  | Pageable  |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Produces: <br />
* application/json
#### Read one month content in given year
```
GET /moneyapp/months/{monthId}?year={yearNumber}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | monthId | month id (not number!) | Integer |
| Path  | year  | year number (not id!) | String |
| Path  | sort  | sort by chosen attribute  | Pageable  |
| Path  | size  | select size  | Pageable  |
| Path  | page  | select page  | Pageable  |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Produces: <br />
* application/json
#### Full month update in given year
```
PUT /moneyapp/months/{monthId}?year={yearNumber}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | monthId | month id (not number!) | Integer |
| Path  | year  | year number (not id!) | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | NoContent |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Part month update in given year
```
PATCH /moneyapp/months/{monthId}?year={yearNumber}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | monthId | month id (not number!) | Integer |
| Path  | year  | year number (not id!) | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | NoContent |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Delete month in given year (only if month content is empty)
```
DELETE /moneyapp/months/{monthId}?year={yearNumber}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | monthId | month id (not number!) | Integer |
| Path  | year  | year number (not id!) | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
### Category section
#### Add category in given year and month (body attributes: type, description) <br />
```
POST /moneyapp/categories?year={yearNumber}&month={monthName}
```
Parameters <br />
| Type  | Name | Description | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | year  | year number (not id!) | String  |
| Path  | month  | month name (in english) | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Created |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Add category with processes in given year and month (body attributes: month, description, list of processes (description, price, currency, transaction type, buy date ("yyyy-MM-dd'T'HH:mm" format), notes)) <br />
```
POST /moneyapp/categories?year={yearNumber}&month={monthName}&processes
```
Parameters <br />
| Type  | Name | Description | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | year  | year number (not id!) | String  |
| Path  | month  | month name (in english) | String |
| Path | categories | - | word 'categories' | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Created |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Add process to category in given year and month (body attributes: description, price, currency, transaction type, buy date ("yyyy-MM-dd'T'HH:mm" format), notes)) <br />
```
POST /moneyapp/categories/{categoryId}?year={yearNumber}&month={monthName}
```
Parameters <br />
| Type  | Name | Description | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | categoryId  | category id (not name!) | Integer |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | name of existing month | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Created |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Read all categories in given year and month  <br />
```
GET /moneyapp/categories?year={yearNumber}&month={monthName}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | String |
| Path  | sort  | sort by chosen attribute  | Pageable  |
| Path  | size  | select size  | Pageable  |
| Path  | page  | select page  | Pageable  | 

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Produces: <br />
* application/json
#### Read all categories + their processes in given year and month <br />
```
GET /moneyapp/categories?year={yearNumber}&month={monthName}&processes
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | String |
| Path | processes | word 'processes' | String |
| Path  | sort  | sort by chosen attribute  | Pageable  |
| Path  | size  | select size  | Pageable  |
| Path  | page  | select page  | Pageable  |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Produces: <br />
* application/json
#### Read one category content in given year and month
```
GET /moneyapp/categories/{categoryId}?year={yearNumber}&month={monthName}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | categoryId | category id (not name!) | Integer |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | String |
| Path  | sort  | sort by chosen attribute  | Pageable  |
| Path  | size  | select size  | Pageable  |
| Path  | page  | select page  | Pageable  |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Produces: <br />
* application/json
#### Full category update in given year and month
```
PUT /moneyapp/categories/{categoryId}?year={yearNumber}&month={monthName}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | categoryId | category id (not name!) | Integer |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | NoContent |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Part category update in given year and month
```
PATCH /moneyapp/categories/{categoryId}?year={yearNumber}&month={monthName}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | categoryId | category id (not name!) | Integer |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | NoContent |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Delete category in given year and month (only if category content is empty)
```
DELETE /moneyapp/categories/{categoryId}?year={yearNumber}&month={monthName}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | categoryId | category id (not name!) | Integer |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
### Process section
#### Read all user processes
```
GET /moneyapp/processes?all
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path  | all  | word 'all' | String |
| Path  | sort  | sort by chosen attribute  | Pageable  |
| Path  | size  | select size  | Pageable  |
| Path  | page  | select page  | Pageable  | 

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Produces: <br />
* application/json
#### Read process in given year, month and category  <br />
```
GET /moneyapp/processes/{processId}?year={yearNumber}&month={monthName}&category={categoryName}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | processId | process id (not name!) | Long |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | String |
| Path  | category  | category name | String | 

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Produces: <br />
* application/json
#### Full process update in given year, month and category  <br />
```
PUT /moneyapp/processes/{processId}?year={yearNumber}&month={monthName}&category={categoryName}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | processId | process id (not name!) | Long |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | String |
| Path  | category  | category name | String | 

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | NoContent |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Part process update in given year, month and category  <br />
```
PATCH /moneyapp/processes/{processId}?year={yearNumber}&month={monthName}&category={categoryName}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | processId | process id (not name!) | Long |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | String |
| Path  | category  | category name | String | 

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | NoContent |
| incorrect validation | Bad request |

Consumes: <br />
* application/json
#### Delete process in given year, month and category  <br />
```
DELETE /moneyapp/processes/{processId}?year={yearNumber}&month={monthName}&category={categoryName}
```
Parameters <br />
| Type  | Name | Description  | Schema |
| ------------- | ------------- | ------------- | ------------- |
| Path | processId | process id (not name!) | Long |
| Path  | year  | year number (not id!) | String |
| Path  | month  | month name (in english) | String |
| Path  | category  | category name | String |

Responses <br />
| Description  | HttpStatus |
| ------------- | ------------- |
| success  | Ok |
| incorrect validation | Bad request |

Consumes: <br />
* application/json

## Build instruction 
Requirements: Git, Java 11, JRE 11 <br />
To run project, follow these steps:
1. Open terminal and clone the project from github repository:
```
$ git clone https://github.com/mkrolczyk12/organizationApp.git
```
2. Write following commands into console:
```
$ .\mvnw clean
$ .\mvnw install
```
In result you should receive application .jar file. <br />
3. Open browser and download keycloak .zip version <br />
Link: https://www.keycloak.org//downloads.html <br />
After download, extract file and move inside your previously cloned project. <br />
Go to folder keycloak-[10.0.1]/bin, open console and write the following command: 
```
windows:
$ standalone.bat -Djboss.socket.binding.port-offset=100
linux:
$ ./standalone.sh -Djboss.socket.binding.port-offset=100
```
Wait until script loaded. From this moment you are able to manage keycloak admin account. <br />
Realm managing page: http://localhost:8180/auth/ <br />
Create admin account and then find the "import" tab in the main menu. Check "realm-export.json" file from cloned project and import.
As a result, you should get pre-prepared realm for your application. <br />
5. Open second terminal and type below commands:
```
$ cd [direct_path_to_the_previously_built_.jar_file]
$ java -jar [name_of_the_jar_file]
```
6. After loading application is ready to use.

## Features
List of features ready and TODOs for future development <br />
Ready features:
* Years section: add year, add single month to chosen year, read single year content, read all years ( + also using [sort,page,size] params), read all years + their months (+ [sort,page,size] params), put year, patch year, delete year (only if the content of the year is empty)
* Months section: add month, add month with categories, add single category to chosen month, read single month content, read all months in given year (+ also using [sort,page,size] params), read all months + their categories (+ [sort,page,size] params) in given year, put month, patch month, delete month (only if the content of the month is empty)
* Categories section: add category, add category with processes, add single process to chosen category, read single category content, read all categories in given year and month (+ also using [sort,page,size] params), read all categories + their processes (+ [sort,page,size] params) in given year and month, put category, patch category, delete category (only if the content of the category is empty)
* Processes section: read single process content, read all user processes (required ?{all} param), read all user processes (?{all} + [sort,page,size] params) put process, patch process, delete process

To-do list:
* Statistics for expenses
* Earnings section + statistics

## Status
_in progress_

## Contact
Created by @mkrolczyk12 - feel free to contact me!
* Phone: (48) 503 699 962
* E-mail: m.krolczyk66@gmail.com
