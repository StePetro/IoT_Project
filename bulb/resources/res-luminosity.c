#include <stdio.h>
#include <stdlib.h>
#include <string.h>

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

RESOURCE(res_presence, "", res_get_handler, res_post_handler, res_put_handler,
         NULL);

/*---------------------------------------------------------------------------*/

static uint8_t lum = 0;
static const uint8_t max_lum = 100;
static const size_t max_char_len = 4;  // Three digits + end string

/*---------------------------------------------------------------------------*/

/* To get the current luminosity value */
static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {
  LOG_INFO("Handling get request...\n");

  char msg[max_char_len];
  snprintf(msg, max_char_len, "%u", lum);
  size_t len = strlen(msg);
  memcpy(buffer, (const void *)msg, len);

  coap_set_header_content_format(response, TEXT_PLAIN);
  coap_set_header_etag(response, (uint8_t *)&len, 1);
  coap_set_payload(response, buffer, len);
}

/*---------------------------------------------------------------------------*/

/* To increase(+) decrease(-) luminosity value */
static void res_post_handler(coap_message_t *request, coap_message_t *response,
                             uint8_t *buffer, uint16_t preferred_size,
                             int32_t *offset) {
  LOG_INFO("Handling post request...\n");

  const char *rcvd_msg = NULL;
  char char_lum[max_char_len];
  size_t len = 0;
  bool bad_request = true;

  len = coap_get_post_variable(request, "+", &rcvd_msg);

  if (len > 0 && len <= max_char_len) {
    memcpy(char_lum, rcvd_msg, len);
    int tmp_lum = lum + atoi(char_lum);

    if (tmp_lum < 0 || tmp_lum > max_lum) {
      LOG_INFO("Received invalid luminosity value\n");
      coap_set_status_code(response, BAD_OPTION_4_02);

    } else {
      lum = tmp_lum;
      LOG_INFO("Luminosity increased to %u\n", lum);
      coap_set_status_code(response, CHANGED_2_04);
    }

    bad_request = false;
  }

  len = coap_get_post_variable(request, "-", &rcvd_msg);

  if (len > 0 && len <= max_char_len) {
    memcpy(char_lum, rcvd_msg, len);
    int tmp_lum = lum - atoi(char_lum);

    if (tmp_lum < 0 || tmp_lum > max_lum) {
      LOG_INFO("Received invalid luminosity value\n");
      coap_set_status_code(response, BAD_OPTION_4_02);

    } else {
      lum = tmp_lum;
      LOG_INFO("Luminosity decreased to %u\n", lum);
      coap_set_status_code(response, CHANGED_2_04);
    }

    bad_request = false;
  }

  if (bad_request) {
    LOG_INFO("Bad Request\n");
    coap_set_status_code(response, BAD_REQUEST_4_00);
  } else {
    char msg[max_char_len];
    snprintf(msg, max_char_len, "%u", lum);
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, len);

    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_header_etag(response, (uint8_t *)&len, 1);
    coap_set_payload(response, buffer, len);
  }
}

/*---------------------------------------------------------------------------*/

/* To set the luminosity value */
static void res_put_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {
  LOG_INFO("Handling put request...\n");

  const char *rcvd_msg = NULL;
  char char_lum[max_char_len];
  size_t len = 0;

  len = coap_get_post_variable(request, "lum", &rcvd_msg);

  if (len > 0 && len <= max_char_len) {
    memcpy(char_lum, rcvd_msg, len);
    int tmp_lum = atoi(char_lum);

    if (tmp_lum < 0 || tmp_lum > max_lum) {
      LOG_INFO("Received invalid luminosity value\n");
      coap_set_status_code(response, BAD_REQUEST_4_00);

    } else {
      lum = tmp_lum;
      LOG_INFO("Luminosity set to %u\n", lum);
      coap_set_status_code(response, CHANGED_2_04);

      /* Send new luminosity value in response */
      char msg[max_char_len];
      snprintf(msg, max_char_len, "%u", lum);
      size_t len = strlen(msg);
      memcpy(buffer, (const void *)msg, len);

      coap_set_header_content_format(response, TEXT_PLAIN);
      coap_set_header_etag(response, (uint8_t *)&len, 1);
      coap_set_payload(response, buffer, len);
    }

  } else {
    LOG_INFO("Bad Request\n");
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
}