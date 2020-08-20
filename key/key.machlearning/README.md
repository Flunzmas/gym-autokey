# The Machine Learning Server

This is a simple socket-based server application that allows a machine
learning approach to interact with KeY.

## Data transfer

Communication works via textually (JSON) represented commands.  over a
socket interface (or a pipe ...)  Data is transferred as JSON
objects. In the following the input `comm(arg=value:str, arg2=42:int)`
is shorthand for the JSON object `{ "command":"comm", "arg":"value",
"arg2":42 }`

For example, the command to load "test.key" would be represented as
`{ "command": "loadFile", "filename": "test.key" }`.

For readability, it is shown here as "loadFile(filename:str)".


In case of the result, the master key is not `command` but
`response`. That is, `error(message="wrong":str)` is, hence,
`{ "response":"error", "message":"wrong" }`

In the error case, the server may choose to add the exception class to
the returned object. The stacktrace is only shown on the server's
console.

## Communication between machine learning tool and KeY.

The outside tool acts as master and instructs KeY to appy individual
tactics to chosen proof goals.

Feature extraction will mainly happen on the KeY side.

Tactic application must be reversible.

## Commands

Initiated by the learning tool, responses by KeY. may be asynchronous.

### Loading of proof

````
   >    load(filename:str)
   <    success
or <    error(message:str)
````

Discards old proof from memory. load new proof obligation.

### Listing all tactics

````
   >    tactics
   <    success(tactics: str[])
or <    error(message:str)
````

### Executing a tactic

````
   >    execute(id: int, tactic: str)
   <    success(ids: int[])
or <    error(message:str)
````

in particular: error if obligation id is not an open goal
`ids` may be empty if closing

### Setting properties

````
   >    set(property:str, value: str)
   <    success
or <    error(message:str)
````

Currently the following properties are supported:

| Property      | Meaning |
| ------------- |:-------------:| -----:|
| `maxStep`     | maximum number of proof steps executed in tactic execution |
| `timeout`     | timeout (in ms) after which automation terminates |


### Prune a proof / Undo execution

````
   >    rewind(id: int)
   <    success
or <    error(message:str)
````

### Evaluate obligation

Feature extraction

````
   >    features(id: int)
   <    success(id:int, features...)
or <    error(message:str)
````

The resulting objects lists a number of features. Defined below (well,
some day ...)

### Retrieve an AST

AST transfer

````
   >    ast(id: int)
   <    success(id: int, antecedent: term[], succedent: term[]
or <    error(message: str)
````
with `term` the recursive structure `{ "op":"...", "op_class":"...", children:term[] }`.

### Listing all available tactics

````
   >    tactics
   <    success(tactics: str[])
or <    error(message: str)
````

### Quitting the server

````
   >    quit
````
No response. 
The process terminates.


# Invoking the server

The server can be invoked using

    ../gradlew server

on unix-like shells and

    ..\gradlew.bat server
    
on Windows. Add `--args=--screen` if you want to have the KeY window
opened for debugging purposed.

# Invoking the KeY-AI app.

If you start KeY using

    ../gradlew run

the interactive KeY application will be opened with a strategy
installed that makes calls to a socket to know the tactic to apply.

The server must be listening to port `6767` and react to JSON requests
of the form

    [ "id": 1234, "antecedent": term[], "succedent": term[] ]\n
    
with term the AST structure described above.

The expected answer of the server is a single word: the name of the
tactic to apply

     AUTO\n
     
 Do not forget the closing `\n`.

# Tactic definitions

The rulesets for the tactics are defined in the file
`src/main/resources/org/key_project/machlearning/filterRulesets.xml`
