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


## Step 2
Pretty straight forward:
- added a command line switch to `CommandLineHelper` for getting the name of the file containing banned hosts
- added a `CommandLineHelper` method w/test to read the file into a `List`
- ammended the `FwdProxy` class to accept the list of banned host names and hand it to the `ProxyHandler` class on construction
- added a method w/test to evaluate a host against the banned list
- had the `FwdProxy` class call the method to see if the host was banned
- had to refactor the `handleInvalidResponse` method to supply a custom message and a `ResponseCode` to allow for better customization of error handling responses.
- added `400 Forbidden` to `ResponseCode`
- updated version number to "0.2.0"

## Step 3
- much the same wiring process for banned hosts:
    - read in with `CommandLineHelper` - some refactoring to make it more generic
    - hand it off to `FwdProxy` for use with `ProxyHandler`
    - create a method to check for banned words and return result
    - sent approperate response if check fails

## Notes

Example Header

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

## Notes
- Wanted to use a dev container enviornment for development but found is slow
