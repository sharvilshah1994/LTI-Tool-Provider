##  LTI TOOL PROVIDER

#### Running the Tool Provider

Make sure you have [Tool Consumer](https://github.com/sharvilshah1994/LTI-Tool-Consumer) installed & running on your System. 

1. Clone this repository to your local system

    ``
    https://github.com/sharvilshah1994/LTI-Tool-Provider.git
    ``
    
2. Go to `
      LTI-Tool-Provider/src/main/resources/application.properties
    ` and make the `consumerKey` & `secret` same as Tool Consumer.

3. Generate the JAR file 

    ``
    cd LTI-Tool-Provider/
    ``
    
    ``mvn clean install``
    
4. Run the application 

    ``java -jar target/toolproducer-0.0.1-SNAPSHOT.jar``
    
Hit the following URL on your browser:

``localhost:8085``

and click on `Send`

You will see the following page (If you have set correct consumerKey & secret):

![alt text](LTIProducer.png)
