#include <stdlib.h>
#include <string.h>

#include "coap-engine.h"

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);
static void res_event_handler(void);

// MODIFICARE LA DESCRIZIONE!
EVENT_RESOURCE(res_presence_detected, "", res_get_handler, NULL, NULL, NULL,
               res_event_handler);

static char presence[6] = "false";

static void res_event_handler(void) {
  if (strcmp(presence, "false")){
    strcpy(presence, "false");
  }else{
     strcpy(presence, "true");
  }

  coap_notify_observers(&res_presence_detected);
}

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {
  char const *const message = presence;
  int length = strlen(message);

  // Copy the response in the transmission buffer
  memcpy(buffer, message, length);

  // Prepare the response
  coap_set_header_content_format(response, TEXT_PLAIN);
  coap_set_header_etag(response, (uint8_t *)&length, 1);
  coap_set_payload(response, buffer, length);
}
