#include <ESP8266WiFi.h>

#define SPEED_PIN 16
#define IN_PIN_1 14
#define IN_PIN_2 12

IPAddress apIP(192,168,4,1);  // Defining a static IP address for the Server

WiFiServer server(80);

void setup() {
    pinMode(SPEED_PIN, OUTPUT);
    pinMode(IN_PIN_1, OUTPUT);
    pinMode(IN_PIN_2, OUTPUT);
    pinMode(LED_BUILTIN,OUTPUT);

    digitalWrite(IN_PIN_1, HIGH);
    digitalWrite(IN_PIN_2, LOW);
    digitalWrite(LED_BUILTIN,LOW);

    Serial.begin(9600);
    while(!Serial);

    //setup the custom IP address
    WiFi.mode(WIFI_AP_STA);

    // COnfigure the Access Point
    //WiFi.softAPConfig(apIP, apIP, IPAddress(255, 255, 255, 0));   // subnet FF FF FF 00
    //WiFi.mode(WIFI_AP);
    Serial.println(WiFi.softAP("ESP-WIFI") ? "Ready" : "Failed!");
    server.begin();
}

void loop() {
    WiFiClient client = server.available();

    Serial.print("Connected, My address: ");
    Serial.print("http://");
    Serial.print(WiFi.softAPIP());
    Serial.println("/");
    if(client == true && client.connected()){
        digitalWrite(LED_BUILTIN,HIGH);
        delay(250);
        digitalWrite(LED_BUILTIN,LOW);
        Serial.println("Client is connected");
    }else{
        digitalWrite(LED_BUILTIN,HIGH);
        delay(250);
        digitalWrite(LED_BUILTIN,LOW);
        delay(250);
        digitalWrite(LED_BUILTIN,HIGH);
        delay(250);
        digitalWrite(LED_BUILTIN,LOW);
        Serial.println("Client is NOT connected");
    }
    delay(1000);
    if (client) {
        while (client.connected()) {
            if (client.available()){
                Serial.write((char)client.read());
            }
        }
    }
    client.stop();
}

void runMotors(){
    digitalWrite(LED_BUILTIN,HIGH);
    analogWrite(SPEED_PIN, 255);

    delay(2000);
    digitalWrite(LED_BUILTIN,HIGH);
    analogWrite(SPEED_PIN, 255 / 1.5);

    delay(2000);
    digitalWrite(LED_BUILTIN,LOW);
    analogWrite(SPEED_PIN, 255 / 2);

    delay(2000);
    digitalWrite(LED_BUILTIN,LOW);
    analogWrite(SPEED_PIN, 255 / 2.5);

    delay(2000);
    digitalWrite(LED_BUILTIN,HIGH);
    analogWrite(SPEED_PIN, 255 / 3);
    delay(2000); 
}

