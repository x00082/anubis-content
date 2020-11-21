local key = KEYS[1]
-- 限流大小
local limitAdd = tonumber(ARGV[1])
local totalAdd = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local total = tonumber(ARGV[4])
local json

local tmp = [[{"limit":0, "total":0}]];
--json = cjson.decode(tmp);

local allLimit = redis.call('get',key);
if(allLimit) then
    json = cjson.decode(allLimit);
    if limit ~= 0 then
        local curentLimit = tonumber(json['limit']) + limitAdd
        if curentLimit > limit then
            return -100;
        else
            json['limit'] = curentLimit
        end
    end

    if total ~= 0 then
        local curentTotal = tonumber(json['total']) + totalAdd
        if curentTotal > total then
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
end

local string = cjson.encode(json);
redis.call('set',key,string);
redis.call("EXPIRE", key, 1)
return 0