# Parallel.com

## Description

The client portion utlizes a basic socket connection to interact with a multithreaded server, supports simultaneously multi clients and excessive load on server causes a graceful degradation instead of an abrupt stop to services.

The server portion makes use of ThreadPools to allow graceful degradation and mutually exclusive locks to coordinate read-write operations

Views are based on fxml declared pages coupled with a controller/handler for each core functionality (e.g Login, various Sends)

Objects are modelled following the Java Beans standard allowing an easy serialization that in turn allows an easy transmission between client and server

With some shame, I have to say that the persistence level is implemented through a folder system (Outgoing, Inbox) that stores single txt files for each mail sent and received

## Functionality

Allows sending mail simultaneously to multiple intraservice accounts

Allows multiple replies

Allows forwarding

Allows mail deletion
