# Coding Challenge 51: Forward Proxy

## Background
Challenge #51 is for a [forward proxy](https://codingchallenges.substack.com/p/coding-challenge-51-http-forward). 

This effort will use:
- Java - the OpenJDK Java implmentation
- Gradle - as the build process - using Groovy as the DSL
- git for source control - pushed up to github

## Step 1
- Useful article on [creating an HTTP server in Java](https://rjlfinn.medium.com/creating-a-http-server-in-java-9b6af7f9b3cd)
- Exposted to [`Optionals`](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) in Swift, but had not ecounterd them in Java before

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
- Wanted to use a dev container envornment for development but found is slow
