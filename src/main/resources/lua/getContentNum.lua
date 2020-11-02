--redis keys
local contentKey =KEYS[1] ; --Lua下表从1开始
--redis args
local timestamp = tonumber(ARGV[1]);
local contentJson = ARGV[2]; --如果redis中没有这个sp的记录，需要将默认配置保存
local todayTimeStamp = tonumber(ARGV[3]); --今天的时间戳，如果redis的时间戳小于该值，说明刷新是昨天，用量需要清零
local json;
local response;
local toSet;
-- 判断用户角色，管理员则不计数

local userLimit = redis.call('get',contentKey);
if(userLimit) then
    json = cjson.decode(userLimit);
    local redisTimeStamp = tonumber(json['lastModify']);
    if(redisTimeStamp < todayTimeStamp) then
        --清空历史用量
        json['urlRefreshNumber']['used'] = 0;
        json['dirRefreshNumber']['used'] = 0;
        json['urlPreloadNumber']['used'] = 0;
        json['lastModify'] = timestamp;
        local response = cjson.encode(json);
        redis.call('set',contentKey,response);
    else
        json['lastModify'] = timestamp;
        response  = cjson.encode(json);
    end
else
    --不存在
    json = cjson.decode(contentJson);
    json['lastModify'] = timestamp;
    response  = cjson.encode(json);
    redis.call('set',contentKey,response);
end
return response;
