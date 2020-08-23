# Selenuim-auto-web
``Codeless Automatc web tasking Gui with selenium`` 

MVC

Maven project using [JavaFx](https://openjfx.io/openjfx-docs/) and [Selenium](https://www.selenium.dev/documentation/en/getting_started/) to run web tasks automaticly

Runs with chrome (Using [WebDriverManager](https://github.com/bonigarcia/webdrivermanager) to manage WebDriver needed by Selenium)

The loading gif is from [Loading.id](https://loading.io/)

Loading screen with preloader from [this tutorial](https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/)

## How to use

[Download](https://github.com/linxuhao/Selenuim-auto-web/releases/latest/download/autoweb.jar) ``Latest Release``

The first start up of the app may takes a little while because

The ``GUI has 3 parts`` : **Action List** at the top left, **output console** at the top right and **button zone** at the bottom

### Action List

A place to edit actions 
#### Actions

Each action has an **Execution Time** : 
- If **not set**, it means execution immediatly
- If **is set to a time at least 1 hours before now**, the execution time will be **same time but tomorrow** (Example : now is 23:55, I'd like to execute some actions at 00:05, this assure that the actions will execute tomorrow 00:05, which is 10 minutes later, 00:05 today is at least 1 hours before 23:55 today)
- If **is set between [now - 1 hours, futur]**, it will be executed **today**

There are ``4 types of actions`` :
- **Navigate** : navigate to an given url
- **Click** : click a element, fuzzy search ordered by id, name, value, text and classname
- **Fill** : fill a input, fuzzy search ordered by id, placeholder, name, value, classname
- **Select** : select a select tag, fuzzy search ordered by id, placeholder, name, value, classname, the select option is based on **visual text**

There are ``3 types of conditions before executing next action`` for (**navigate and click**) :
- **HttpCode** : compare the http response code to know if action is succed, user has to set the wanted http response code
- **Delay** : delay for given milliseconds before executing next action
- **Non condition** : execute next action whenever this action finishes (**Fill and Select** are naturally bind with this option)

``Retry``
- **Navigate** : Re-attemp to do the navigate action
- **Click** : Refresh the current page (Since the retry condition is only http code, it means only hyper link button is affected)

### Button zone

- **+** : add a action at the end of the action list
- **-** : remove the last element from action list
- **Load** : load a previously saved action list from file
- **Save** : save current action list into file
- **Start** : starts to run the actions in the action list on a new navigator
- **Multi launch picker** : launch the same task n times
- **Clear log** : clears the log

### output console
Is simply a console that output colored text telling you about the current state of the running task
