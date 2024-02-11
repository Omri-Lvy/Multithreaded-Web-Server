Annotations: Get,Head, Post, Trace. All of these java classes define custom annotations that provide the meta data about code elements. These classes are used to define which HTTP request methods each routing in our router class supports.

Config:
Configuration: The Config class is an object that defines all the configurations fields for the server.
ConfigurationManager: This class parses the config file and creates a new configuration object for the server to pull the settings from.

enums:
ContentType: Predefine the content types that our server supports.
HttpMethod: Predefined all types of HTTP methods in order to check if the request method is implemented or not in our server.
HttpStatusCode: Predefined all the respond status codes that our server supports.
HttpVersion: Predefined the HTTP versions that our server supports.

Exceptions:
HttpParsingException and HttpProcessingException: Defining custom exception that we will raise during the server running time where needed.

http:
HttpRequest: A class that defines an object to hold a request's data such as request body, header, methods, http methods and the request's target.
HttpResponse: A class containing all the functions that handles sending the response to the user.

Routing:
Controller: Defining all the routes and methods for each route that our server supports and how to handle each request.
Router: A class that checks invokes the relevant methods according to the request's target and method.

Server:
ClientConnectionHandler: Handling the client connection including getting the request, parsing it and forwarding to the router to respond to it accordingly.
ServerListener: Holds the threadpool in order to limit the number of connections our server allows. Opening a new thread for each connection and rejecting new connections if the limit is met.

utils:
HttpRequestParser: Parsing all the request parts and returning a new HttpRequest object.



WebServer:
The main class of the server. Responsible to run the server on the configurations given in the config.ini file and close the server appropriately when needed.


We choose to design the project in a way that divides responsibilities to different classes. Giving each class a specific responsibility to handle to ensure encapsulation and readability while promoting extensibility and maintainability.
Using this structure enables us to support various types of HTTP request while keeping in mind future request types, file types, content types and routes that we might like to add in the future.
Additionally this structures enables us to handle error and exceptions better because each class throws its own exceptions. which allows us to handle the request in the best way possible and retiring the correct response status.