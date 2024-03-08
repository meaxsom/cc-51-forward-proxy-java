# Coding Challenge 51: Forward Proxy

## Background
Challenge #51 is for a [forward proxy](https://codingchallenges.substack.com/p/coding-challenge-51-http-forward). 

This effort will use:
- Java - an OpenJDK Java implmentation version 11 or better
- Gradle - as the build process - using Groovy as the DSL
- git for source control - pushed up to github

## Step 1
- Useful article on [creating an HTTP server in Java](https://rjlfinn.medium.com/creating-a-http-server-in-java-9b6af7f9b3cd)
- Exposed to [`Optionals`](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) in Swift, but had not ecounterd them in Java before
- [httpbin.org](http://httpbin.org/) is a nice HTTP/HTTPS Request & Response Service


## Step 2 Banned Hosts
Pretty straight forward:
- added a command line switch to `CommandLineHelper` for getting the name of the file containing banned hosts
- added a `CommandLineHelper` method w/test to read the file into a `List`
- ammended the `FwdProxy` class to accept the list of banned host names and hand it to the `ProxyHandler` class on construction
- added a method w/test to evaluate a host against the banned list
- had the `FwdProxy` class call the method to see if the host was banned
- had to refactor the `handleInvalidResponse` method to supply a custom message and a `ResponseCode` to allow for better customization of error handling responses.
- added `400 Forbidden` to `ResponseCode`
- updated version number to "0.2.0"


## Step 3: Banned Words
- much the same wiring process for banned hosts:
    - read in with `CommandLineHelper` - some refactoring to make it more generic
    - hand it off to `FwdProxy` for use with `ProxyHandler`
    - create a method to check for banned words and return result
    - sent approperate response if check fails


## Step 4: Log Activity
- log activity to a file and console
    - use `Log4J` to set up a console and access log config tied to a class
        - modify based on preference
    - create `AccessLogger` class to handle standard types of logging
    - profit!

## Step 5: HTTPS Tunnel
- Request has `CONNECT` instead of `GET` as the HTTP `verb` that tells the server it needs an HTTPS tunnel.
    - [Handling CONNECT messsage from client](https://greenbytes.de/tech/webdav/draft-ietf-httpbis-p2-semantics-26.html#CONNECT)
    - [Responding to a CONNECT](https://stackoverflow.com/questions/28495938/how-do-i-respond-to-a-connect-method-request-in-a-proxy-server-using-socket-in)
    
- Ended up being simpler than I thought but I over complicated the solution initially and had to find my way back to a simplier path.
- Hooking the host/port up to my browser it's successfully proxying browser requests!
- I think we're done here!!

## Notes
- Wanted to use a Java dev container enviornment for development but found is slow

- for server reading from ssl socket
    - [SSL/TSL Socket Overlay](https://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLSocketFactory.html#createSocket%28java.net.Socket,%20java.lang.String,%20int,%20boolean%29)
    - [SSL Server Socket Example](https://stackoverflow.com/questions/47068155/is-it-possible-to-use-java-serversocket-to-accept-https-requests)
    - [SSL/TSL Server Code Sample](http://www.java2s.com/Tutorial/Java/0490__Security/HttpsSocketClient.htm)

- [for tunneling](https://github.com/mukatee/java-tcp-tunnel/blob/master/src/net/kanstren/tcptunnel/forwarder/TCPTunnel.java)
- [simpler tunneling](https://stackoverflow.com/questions/18273703/tunneling-two-socket-client-in-java)

- Example HTTP Request w/Headers

```
GET /kl;sjdfjskl/sdfasdf.xml?peter=bob&jack=jill HTTP/1.1
Host: localhost:8989
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:123.0) Gecko/20100101 Firefox/123.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8
Accept-Language: en-US,en;q=0.5
Accept-Encoding: gzip, deflate, br
Connection: keep-alive
Upgrade-Insecure-Requests: 1
Sec-Fetch-Dest: document
Sec-Fetch-Mode: navigate
Sec-Fetch-Site: none
Sec-Fetch-User: ?1
```

