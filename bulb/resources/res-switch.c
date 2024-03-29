#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "coap-engine.h"
#include "os/dev/leds.h"

// "status" variable
#include "global-variables.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Bulb Switch Resource"
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

RESOURCE(res_switch, 
         "</bulb/switch>;title=\"Bulb Switch\";rt=\"switch\";if=\"bulb\"", 
         res_get_handler, 
         res_post_handler, 
         res_put_handler,
         NULL);

/*---------------------------------------------------------------------------*/

// 1 = ON, 0 = OFF
bool status = 1;
static const size_t max_char_len = 3;  // Three digits maximum

/*---------------------------------------------------------------------------*/

/* To get the current switch value */
static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {
  LOG_INFO("Handling get request...\n");

  char msg[max_char_len];

  if (status == 1) {
    snprintf(msg, max_char_len, "%s", "ON");
  } else {
    snprintf(msg, max_char_len + 1, "%s", "OFF");  // +1 = end string
  }

  size_t len = strlen(msg);
  memcpy(buffer, (const void *)msg, len);

  coap_set_header_content_format(response, TEXT_PLAIN);
  coap_set_header_etag(response, (uint8_t *)&len, 1);
  coap_set_payload(response, buffer, len);
}

/*---------------------------------------------------------------------------*/

/* Toggle ON <-> OFF */
static void res_post_handler(coap_message_t *request, coap_message_t *response,
                             uint8_t *buffer, uint16_t preferred_size,
                             int32_t *offset) {
  LOG_INFO("Handling post request...\n");

  if (status == 0) {
    LOG_INFO("Switch set to ON\n");
    leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
  } else {
    LOG_INFO("Switch set to OFF\n");
    leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
  }

  // toggle
  status = !status;

  coap_set_status_code(response, CHANGED_2_04);
}

/*---------------------------------------------------------------------------*/

/* To set the switch on or off */
static void res_put_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {
  LOG_INFO("Handling put request...\n");

  const char *rcvd_msg = NULL;
  char char_on_off[max_char_len];
  size_t len = 0;

  len = coap_get_post_variable(request, "status", &rcvd_msg);

  if (len > 0 && len <= max_char_len) {
    // correct len
    snprintf(char_on_off, max_char_len + 1, "%s", rcvd_msg);  // +1 = end string

    if (strcmp(char_on_off, "ON") == 0) {
      // correct ON request, notify new status
      status = 1;
      LOG_INFO("Switch set to ON\n");
      leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
      coap_set_status_code(response, CHANGED_2_04);

      char msg[max_char_len];

      snprintf(msg, max_char_len, "%s", "ON");

      size_t len = strlen(msg);
      memcpy(buffer, (const void *)msg, len);

      coap_set_header_content_format(response, TEXT_PLAIN);
      coap_set_header_etag(response, (uint8_t *)&len, 1);
      coap_set_payload(response, buffer, len);
    }

    if (strcmp(char_on_off, "OFF") == 0) {
      // correct OFF request, notify new status
      status = 0;
      LOG_INFO("Switch set to OFF\n");
      leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
      coap_set_status_code(response, CHANGED_2_04);

      char msg[max_char_len];
      snprintf(msg, max_char_len + 1, "%s", "OFF");  // +1 = end string

      size_t len = strlen(msg);
      memcpy(buffer, (const void *)msg, len);

      coap_set_header_content_format(response, TEXT_PLAIN);
      coap_set_header_etag(response, (uint8_t *)&len, 1);
      coap_set_payload(response, buffer, len);
    }

  } else {
    // incorrect request
    LOG_INFO("Bad Request\n");
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
}