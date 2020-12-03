contikier
cd tools/cooja
ant run

-- Da usare nella cartella del border router:
make TARGET=cooja connect-router-cooja

coap-client -m get coap://[fd00::202:2:2:2]/