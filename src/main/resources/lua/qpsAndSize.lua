local key = KEYS[1]
-- 限流大小
local limitAdd = tonumber(ARGV[1])
local totalAdd = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local total = tonumber(ARGV[4])
local expire = tonumber(ARGV[5] or "1")
local json

local tmp = [[{"limit":0, "total":0}]];
--json = cjson.decode(tmp);

local allLimit = redis.call('hget',key, "qpsAndSize");
if(allLimit) then
    json = cjson.decode(allLimit);
    if limit ~= 0 then
        local curentLimit = tonumber(json['limit']) + limitAdd
        if curentLimit > limit then
            local ttl = redis.call('ttl',key);
            if ttl > expire or ttl == -1 then
                redis.call("EXPIRE", key, expire)
            end
            return -100;
        else
            json['limit'] = curentLimit
        end
    end

    if total ~= 0 then
        local curentTotal = tonumber(json['total']) + totalAdd
        if curentTotal > total then
            if ttl > expire or ttl == -1 then
                redis.call("EXPIRE", key, expire)
            end
            return -200;
        else
            json['total'] = curentTotal
        end
    end
else
    --local tmp = [[{"limit":0, "total":0}]];
    json = cjson.decode(tmp);
    json['limit'] = limitAdd
    json['total'] = totalAdd

    local string = cjson.encode(json);
    redis.call('hset', key, "qpsAndSize", string);
    redis.call("EXPIRE", key, expire)
    return 0
end

local string = cjson.encode(json);
redis.call('hset', key, "qpsAndSize", string);
local ttl = redis.call('ttl',key);
if ttl > expire or ttl == -1 then
    redis.call("EXPIRE", key, expire)
end
return 0