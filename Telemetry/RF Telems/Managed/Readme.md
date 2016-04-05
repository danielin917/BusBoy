
# Telemetry in C Sharp

This is a library for receiving telemetry messages sent over the Python DataBus in Managed Code (such as C# or C++/CLI).

## Introduction

#### Solution Layout

The __Telemetry__ solution contains three projects:
 * __DataBus__: The main project, which actually contains the `DataBus` class. This is the project you will reference from your own solutions.
 * __DataBusTest__: This project contains a unit test suite. For more information, see __testing__ below. 
 * __Example__: An example project that uses the Data Bus. This is the same code as you will see in this Readme.

#### Setup

Using the Data Bus in your other managed code solutions is relatively easy. Just do the following:
 1. __Add the project__. In your solution, go to `File`>`Add`>`Existing Project` and select the DataBus project. Be careful, though; this will add a path that will work on _your computer only_. To make it work on every computer, open your solution's `.sln` file, find where it includes the `DataBus` project, and replace the path with one relative to the `%solartelemetry` environment variable.
 2. __Add a dependency__. Go to `Project`>`Project Dependencies...` and check the box for `DataBus`. This will ensure that if someone makes changes to the DataBus (including you), the changes will be incorporated into your executable.
 3. __Add a reference__. Go to `Project`>`Add Reference`>`Solution`>`Projects	`, and check the box for `DataBus`. It is very important that you select the project from the Solution/Projects menu, rather than clicking Browse and navigating to the `.dll` file. This way, you will have a Debug dll when you build your project in Debug mode and a Release dll when you build your project in Release mode.

#### Using the Data Bus

First, here's a complete example:

```c#
using System;
using Solar;

class Program
{
    static void Main(string[] args)
    {
        DataBus bus = new DataBus("localhost", 10000);
        bus.subscribe("bms_data", CallbackFunction);
    }
    static void CallbackFunction(Message message)
    {
        Console.WriteLine(message.ToString());
    }
}
```
This program will listen for any Value updates on the `bms_data` record, and print them. 

Now, let's look at the signifigant parts of the code:
```c#
using Solar;
```
The Data Bus is in the `Solar` namespace, meaning once you have followed the steps in __Setup__ above, this is all you have to do allow your code to use the `DataBus` class and associated classes.
```c#
DataBus bus = new DataBus("localhost", 10000);
```
Construct a new `DataBus` object. The first parameter is the hostname or IP address of the machine we want to connect to, and the second is the port number of the Python data_bus running on that machine. In this case, we connect to ourselves on port 10000.
```c#
bus.subscribe("bms_data", CallbackFunction);
```
Here, we _subscribe_ to receive a record. `Subcribe` takes two parameters: the name of a Record, and a function. Every time a new Value for that Record is received, the function you specified will _automatically_ be  called. This is how you receive messages on the Data Bus. In this case, we are saying we want "`CallbackFunction`" (defined later) to be called whenever a Value for the "`bms_data`" record is received.
```c#
static void CallbackFunction(Message message)
{
	Console.WriteLine(message.ToString());
}
```
Here, we define the `CallbackFunction` we talked about earlier. Notice that it takes one parameter, a `Message` object. `Message` is another class defined in the Data Bus; it contains one particular set of Values for any record, plus some metadata about the record. To be able to pass a function to `DataBus.subscribe`, it must have the same signature as this function (that is, it must take a single `Message` object and return `void`). In this case, our function is very simple: it just writes converts the message to a string and writes it to the console.

## Complete Documentation

#### DataBus

```c#
public DataBus()
```
Construct a `DataBus` object. Takes no arguments, and doesn't connect to any remote host (use the `connect()` function later).

```c#
public DataBus(string hostname, uint port)
```
Construct a `DataBus` object, and connect to a remote host. 
 * `hostname`: The URL, IP address, or hostname of the remote Python data_bus server.
 * `port`: The port of the remote Python data_bus server.

```c#
public void connect(string hostname, uint port)
```
Connect to a remote host. If already connected to a remote host (either by using the non-default constructor or another call to the `connect()` function), disconnects from that remote host before connecting.
 * `hostname`: The URL, IP address, or hostname of the remote Python data_bus server.
 * `port`: The port of the remote Python data_bus server.

```c#
public void disconnect()
```
Disconnect from the current remote host, if connected, and clean up connection resources. If not connected to any host, does nothing.

```c#
void subscribe(string recordName, TMessageDelegate mdelegate)
```
Subscribe to receive messages for a particlar record. Whenever new Values for record `recordName` are received, function `mdelegate` will be called.
 * `recordName`: a string specifying the name of the Record to which you want to subscribe.
 * `mdelegate`: a `TMessageDelegate` to invoke every time a Value for the record with name `recordName` is received. Functions can be used as this parameter, but they must take one argument, an instance of `Message`, and return void.

```c#
void subscribeAll(TMessageDelegate mdelegate)
```
Like `subscribe`, but subscribes to _all_ records, rather than just `recordName`. 
 * `mdelegate`: a `TMessageDelegate` to invooke every time a Value is received. Functions can be used as this parameter, but they must take one argument, an instance of `Message`, and return void.

```c#
void subscribeRecords(TRecordDelegate rdelegate)
```
Subscribe to be notified when new Records, not Values, are received. The Data Bus will call `rdelegate` whenever a new Record is received. This function is useful mainly if you are attempting to implement some sort of generic viewer, because this will allow you to know all the records there are. Care should be taken with this function, however; if there are records that have already been received before this function is called for the first time, `rdelegate` will not be called for those Records. Thus, if using this function, you should use the default constructor `DataBus()`, make all the calls you need to `subscribeRecords`, and then call `DataBus.connect(hostname, port)`. That way, you can guarantee no records will be received until after you have subscribed your functions. If you call this, you may also want to call `subscribeFlush()`, so that you know when records are cleared.
 * `rdelegate`: a `TRecordDelegate` to invoke every time a new record is received. Functions can be used for this parameter, but they must return `void` and take a single parameter of type `RecordDescriptor`.

```c#
void subscribeFlush(TFlushDelegate fdelegate)
```
Subscribe to be notified when Records are flushed. This means that the Python data_bus has requested that all records be forgotten about, so that they can be replaced with new Records. If you call `subscribeRecords()`, you may also want to call this. Flush subscribers are always called _before_ record subscribers with the Version record.
 * `fdelegate`: a `TFlushDelegate` to invoke every time a flush record is received. Functions can be used as this parameter, but they must return `void` and take no parameters.

#### Message

```c#
public dynamic this[string valueName]
```
An overload for the square brackets operator. Using the square brackets on a `Message` object will access the Value of the record in question. This function has a return type of `dynamic`, meaning its return type is determined at runtime. It returns a message of the C# type that corresponds to the underlying telemetry message. At the end of this document is a table giving the relations between underlying telemetry types and C# types.
 * `valueName`: The Value member you would like to retrieve.

```c#
Dictionary<string, dynamic> values;
```

A read-only hashmap that contains the underlying representations of the various Value members. The square bracket operator above is just a shorthand for accessing this dictionary; `Message.values[valueName]` also works. This dictionary is useful because it allows you to perform more generic operations (such as iterating over all the Value elements, or copying them to your own structure).

```c#
string ToString()
```
Converts the Message to a string format, suitable for pretty printing. Useful for debugging.

```c#
RecordDescriptor meta;
```
A read-only variable that contains metadata about the Record to which this Value pertains. See __RecordDescriptor__ below for the methods and properties on this object.

#### RecordDescriptor
A class which contains metadata regarding any particular Record. All data is stored in properties, and the class is read-only.
```c#
string description;
```
A description of the Record's purpose, as specified in the REC_ message transmitted from the Python data_bus.

```c#
string name;
```
The name of this Record - this is the same as the `string recordName` parameter used in the `subscribe` function. Useful if you have one function that is subscribed to multiple Records, so that you can determine which record was actually received.

```c#
uint code;
```
The unique identifier for the Record used in the Python data_bus transmissions. Mostly useless outside of `DataBus`, but provided for debugging.

```c#
Dictionary<string, ValueDescriptor> harness;
```
A property giving metadata about _each individual value_, rather than about the whole record. The key is a Value member name. For more information about the values, see __ValueDescriptor__ below.

#### ValueDescriptor

A class which contains metadata regarding any particular Value in a record. All data is stored in properties, and the class is read-only.

```c#
Type type;
```
The Type of this value. Always a member of the `ValueDescriptor.Type` enum:
```c#
public enum Type
{
  uintType,
  intType,
  floatType
}
```
`uintType` is an Unsigned Integer, `intType` is a signed integer, and `floatType` is a single floating-point value.

```c#
string typename;
```
Much like the `type` property, but returns the string version of the type, as specified in the Record transmitted by the Python data_bus.

```c#
uint offset;
```
The offset of this value in the `VAL_` message sent by the Python data_bus. Mostly useless outside of the `DataBus`, but provided for debugging.

```c#
uint size;
```
The size of this value, in bytes. Combining this with the Type stored in the `type` property gives the actual type of the Value member, as described in the table at the end of this document.

```c#
string description;
```
The description of this Value member, as specified in the Record transmitted by the Python data_bus.

## Value Types

Each Value member in a Telemetry message has a type (int, uint, float) and a size in bytes. This table gives a mapping from these dynamically-defined types to C# types.

| size 	| float 	| int   	| uint   	|
|------	|-------	|-------	|--------	|
| 1    	|       	| sbyte 	| byte   	|
| 2    	|       	| Int16 	| UInt16 	|
| 4    	| float 	| Int32 	| UInt32 	|
| 8    	| double   	| Int64 	| UInt64 	|

## Thread Safety
The DataBus is thread-safe (with the exception of multiple calls to `connect()` or `disconnect()`, see below).  Here's some more details regarding thread safety for individual methods.

 * __`subscribe`__, __`subscribeAll`__, __`subscribeRecord`__, and __`subscribeFlush`__: these methods are fully thread safe and can be called at any time. Once called, callbacks will be triggered every time the data bus receives a new Value, Record or Flush, respectively. However, callbacks will not be triggered for Values or Records which have already been received. Thus, if you want to receive all Records, you must call `subscribeRecord()` before calling `connect()`. If multiple callbacks are subscribed to the same thing, the order in which they will be called is undefined (but they will all be called). If a new Version Record (which triggers a flush) is received, Flush subscribers will be called _before_ Record subscribers for the version record. Values cannot be received until the record to which that value corresponds has been received, and all Record subscribers have been called.
 * __`Message`__ and __`RecordDescriptor`__: these objects are read-only, and reading them is fully thread-safe. Objects remain unchanged even if new Values or Records are received to replace them.
 * __`connect`__, __`disconnect`__: these methods may be called even while a callback is running. If a callback is running, all callbacks for that Value or Record will be finished, but no callbacks for new records will be fired. __Warning:__ calling `connect()` or `disconnect()` simultaneously, or calling more than one instance of `connect()` simultaneously, is __not__ thread safe.


## Todo
 * Add function to retrieve a list of all record, and add function to check the current value of a record