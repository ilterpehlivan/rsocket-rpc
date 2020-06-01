package io.rsocket.rpc.core.extension.metadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum CustomMimeType {
  UNPARSEABLE_MIME_TYPE("UNPARSEABLE_MIME_TYPE_DO_NOT_USE", (byte) -2),
  UNKNOWN_RESERVED_MIME_TYPE("UNKNOWN_YET_RESERVED_DO_NOT_USE", (byte) -1),

  // customized data type
  // customized rsocket related types
  MESSAGE_RSOCKET_BINARY_ROUTING("message/x.rsocket.binary-routing.v0", (byte) 0x50),
  MESSAGE_RSOCKET_APPLICATION("message/x.rsocket.application+json", (byte) 0x51),
  MESSAGE_RSOCKET_DATA_CACHE_CONTROL("message/x.rsocket.rsocket.data.cache-control", (byte) 0x53),
  MESSAGE_RSOCKET_SERVICE_REGISTRY("message/x.rsocket.service.registry.v0+json", (byte) 0x54),
  MESSAGE_RSOCKET_MESSAGE_TAGS("message/x.rsocket.message.tags", (byte) 0x57),
  MESSAGE_RSOCKET_MESSAGE_ORIGIN("message/x.rsocket.message.origin", (byte) 0x58),

  MESSAGE_RSOCKET_MIMETYPE("message/x.rsocket.mime-type.v0", (byte) 0x7A),
  MESSAGE_RSOCKET_ACCEPT_MIMETYPES("message/x.rsocket.accept-mime-types.v0", (byte) 0x7B),

  //RPC MIME_TYPES
  MESSAGING_RPC_SERVICE_NAME_MIMETYPE("message/x.rpc.service", (byte) 0x80),
  MESSAGING_RPC_METHOD_NAME_MIMETYPE("message/x.rpc.method", (byte) 0x81),
  MESSAGING_RPC_HEADERS_MIMETYPE("message/x.rpc.headers", (byte) 0x82);


  static final CustomMimeType[] TYPES_BY_MIME_ID;
  static final Map<String, CustomMimeType> TYPES_BY_MIME_STRING;

  static {
    // precompute an array of all valid mime ids, filling the blanks with the RESERVED enum
    TYPES_BY_MIME_ID = new CustomMimeType[128]; // 0-127 inclusive
    Arrays.fill(TYPES_BY_MIME_ID, UNKNOWN_RESERVED_MIME_TYPE);
    // also prepare a Map of the types by mime string
    TYPES_BY_MIME_STRING = new HashMap<>(128);

    for (CustomMimeType value : values()) {
      if (value.getIdentifier() >= 0) {
        TYPES_BY_MIME_ID[value.getIdentifier()] = value;
        TYPES_BY_MIME_STRING.put(value.getString(), value);
      }
    }
  }

  private final byte identifier;
  private final String str;

  CustomMimeType(String str, byte identifier) {
    this.str = str;
    this.identifier = identifier;
  }

  /**
   * Find the {@link CustomMimeType} for the given identifier (as an {@code int}). Valid identifiers
   * are defined to be integers between 0 and 127, inclusive. Identifiers outside of this range will
   * produce the {@link #UNPARSEABLE_MIME_TYPE}. Additionally, some identifiers in that range are
   * still only reserved and don't have a type associated yet: this method returns the {@link
   * #UNKNOWN_RESERVED_MIME_TYPE} when passing such an identifier, which lets call sites potentially
   * detect this and keep the original representation when transmitting the associated metadata
   * buffer.
   *
   * @param id the looked up identifier
   * @return the {@link CustomMimeType}, or {@link #UNKNOWN_RESERVED_MIME_TYPE} if the id is out of
   *     the specification's range, or {@link #UNKNOWN_RESERVED_MIME_TYPE} if the id is one that is
   *     merely reserved but unknown to this implementation.
   */
  public static CustomMimeType fromIdentifier(int id) {
    if (id < 0x00 || id > 0x7F) {
      return UNPARSEABLE_MIME_TYPE;
    }
    return TYPES_BY_MIME_ID[id];
  }

  /**
   * Find the {@link CustomMimeType} for the given {@link String} representation. If the
   * representation is {@code null} or doesn't match a {@link CustomMimeType}, the {@link
   * #UNPARSEABLE_MIME_TYPE} is returned.
   *
   * @param mimeType the looked up mime type
   * @return the matching {@link CustomMimeType}, or {@link #UNPARSEABLE_MIME_TYPE} if none matches
   */
  public static CustomMimeType fromString(String mimeType) {
    if (mimeType == null) throw new IllegalArgumentException("type must be non-null");

    // force UNPARSEABLE if by chance UNKNOWN_RESERVED_MIME_TYPE's text has been used
    if (mimeType.equals(UNKNOWN_RESERVED_MIME_TYPE.str)) {
      return UNPARSEABLE_MIME_TYPE;
    }

    return TYPES_BY_MIME_STRING.getOrDefault(mimeType, UNPARSEABLE_MIME_TYPE);
  }

  /** @return the byte identifier of the mime type, guaranteed to be positive or zero. */
  public byte getIdentifier() {
    return identifier;
  }

  /**
   * @return the mime type represented as a {@link String}, which is made of US_ASCII compatible
   *     characters only
   */
  public String getString() {
    return str;
  }

  /** @see #getString() */
  @Override
  public String toString() {
    return str;
  }
}
