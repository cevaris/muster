namespace java com.muster.thriftjava
#@namespace scala com.muster.thriftscala

typedef string UUID
typedef string DateTime

struct Context {
    1: required UUID id
    2: required DateTime datetime //ISO8601 format
}

enum MusterCacheStatus {
    OK = 1,
    KEY_NOT_FOUND = 2
}

struct MusterCachePutRequest {
    1: required Context context
    2: required string key
    3: required list<byte> value
}

struct MusterCachePutResponse {
    1: required Context context
    2: required MusterCacheStatus status
}

struct MusterCacheGetRequest {
    1: required Context context
    2: required string key
}

struct MusterCacheGetResponse {
    1: required Context context
    2: required MusterCacheStatus status
    3: required string key
    4: optional list<byte> value
}

service MusterCacheService {
    MusterCachePutResponse put(1: MusterCachePutRequest request)
    MusterCacheGetResponse get(1: MusterCacheGetRequest request)
}