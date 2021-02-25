contikier
cd tools/cooja
ant run

make TARGET=cooja connect-router-cooja

-- Da usare nella cartella del border router:
make TARGET=cooja connect-router-cooja

coap-client -m get coap://[fd00::202:2:2:2]/RISORSA

coap-client -m put coap://[fd00::202:2:2:2]/RISORSA -e PARAMETRO=10

coap-client -m post coap://[fd00::202:2:2:2]/RISORSA -e PARAMETRO1=10\&PARAMETRO2=11

coap-client -m get coap://[fd00::202:2:2:2]:5683/RISORSA_OSSERVABILE -s 100


---------------------BULB------------------------------

----- SWITCH

coap-client -m get coap://[fd00::202:2:2:2]/switch 

coap-client -m post coap://[fd00::202:2:2:2]/switch 

coap-client -m put coap://[fd00::202:2:2:2]/switch -e status=ON
coap-client -m put coap://[fd00::202:2:2:2]/switch -e status=OFF

----- LUMINOSITY

coap-client -m get coap://[fd00::202:2:2:2]/luminosity

coap-client -m post coap://[fd00::202:2:2:2]/luminosity -e +=10
coap-client -m post coap://[fd00::202:2:2:2]/luminosity -e -=10

coap-client -m put coap://[fd00::202:2:2:2]/luminosity -e lum=100