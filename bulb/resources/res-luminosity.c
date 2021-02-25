#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "coap-engine.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Bulb Luminosity Resource"
#define LOG_LEVEL LOG_LEVEL_INFO

/*---------------------------------------------------------------------------*/

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

static void res_post_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

static void res_put_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

RESOURCE(res_luminosity, "",
         res_get_handler, res_post_handler, res_put_handler, NULL);

/*---------------------------------------------------------------------------*/

static uint8_t lum = 0;
static const uint8_t max_lum = 100;
static const size_t max_char_len = 3; // Three digits maximum

/*---------------------------------------------------------------------------*/

/* To get the current luminosity value */
static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

  LOG_INFO("Handling get request...\n");

  char msg[max_char_len];
  snprintf(msg,max_char_len,"%u",lum);
  size_t len = strlen(msg);
  memcpy(buffer, (const void*) msg, len);

  coap_set_header_content_format(response, TEXT_PLAIN);
  coap_set_header_etag(response, (uint8_t *)&len, 1);
  coap_set_payload(response, buffer, len);

}

static void res_post_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

  const char *text = NULL;

  size_t len = 0;
  char room[15];
  memset(room, 0,15);

  char temp[32];
  memset(temp,0,32);
  

  len = coap_get_post_variable(request, "name", &text);
  if(len > 0 && len <= max_char_len){
    memcpy(room, text, len);
  }

  len = coap_get_post_variable(request, "value", &text);
  if(len > 0 && len < 15){
    memcpy(temp, text, len);
  }

  printf("Temperatura settata in %s a %s °C", room, temp);

}

/* To set the luminosity value */
static void res_put_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

  LOG_INFO("Handling put request...\n");

  const char *rcvd_msg = NULL;
  char char_lum[max_char_len];
  size_t len = 0;


  len = coap_get_post_variable(request, "lum", &rcvd_msg);
  if(len > 0 && len <= max_char_len){
    memcpy(char_lum, rcvd_msg, len);
  }

  //lum = atoi(char_lum);

  LOG_INFO("Luminosity set to %s\%\n", char_lum);

}