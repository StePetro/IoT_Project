#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "coap-engine.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Register Resource"
#define LOG_LEVEL LOG_LEVEL_INFO

// Max block size = 2048
#define MAX_DEVICES 40
#define MAX_IP_LEN 39
#define MAX_TYPE_LEN 10
#define MAX_DEVICE_DESCRIPTOR_LEN 50 //type-ip

/*---------------------------------------------------------------------------*/

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

static void res_put_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

RESOURCE(res_register, "",
         res_get_handler, NULL, res_put_handler, NULL);

/*---------------------------------------------------------------------------*/

static char reg[MAX_DEVICES][MAX_DEVICE_DESCRIPTOR_LEN];
static int devices_count = 0;

/*---------------------------------------------------------------------------*/

/* To get registred devices */
static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

  LOG_INFO("Handling get request...\n");

  char msg[MAX_DEVICES*MAX_DEVICE_DESCRIPTOR_LEN + MAX_DEVICES] = ""; // need to count also the " "

  /* Concat list of available devices */
  for(int i=0; i < devices_count; i++){

    if(i == 0){
      sprintf(msg, "%s", reg[i]);
    }else{
      sprintf(msg, "%s %s", msg, reg[i]);
    }

  }

  size_t len = strlen(msg);
  memcpy(buffer, (const void*) msg, len);

  coap_set_header_content_format(response, TEXT_PLAIN);
  coap_set_header_etag(response, (uint8_t *)&len, 1);
  coap_set_payload(response, buffer, len);

}

/*---------------------------------------------------------------------------*/

/* Add device to register */
static void res_put_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

  LOG_INFO("Handling put request...\n");

  if(devices_count == MAX_DEVICES){
    LOG_INFO("Register full\n");
    coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
    return;
  }

  const char *rcvd_msg = NULL;
  char type[MAX_TYPE_LEN];
  size_t len = 0;
  bool ok = false;

  len = coap_get_post_variable(request, "type", &rcvd_msg);
  if (len > 0 && len <= MAX_TYPE_LEN) {

    snprintf(type, len + 1, "%s", rcvd_msg);
    
    len = coap_get_post_variable(request, "ip", &rcvd_msg);
    if(len > 0 && len <= MAX_IP_LEN){
      snprintf(reg[devices_count], len+strlen(type)+2, "%s@%s",type, rcvd_msg);
      ok = true;
    }

  }

  if(ok){

    devices_count++;
    LOG_INFO("%s added to register, total devices: %u\n", reg[devices_count - 1], devices_count);
    coap_set_status_code(response, CHANGED_2_04);

  }else{

    LOG_INFO("Bad request\n");
    coap_set_status_code(response, BAD_REQUEST_4_00);

  }

}