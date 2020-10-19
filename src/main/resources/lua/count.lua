--redis keys
local contentKey =KEYS[1] ; --Lua下表从1开始
--redis args
local adminFlag = ARGV[1];
local type = ARGV[2]; -- url/dir/preheat
local size = ARGV[3] ;
local timestamp = tonumber(ARGV[4]);
local contentJson = ARGV[5]; --如果redis中没有这个sp的记录，需要将默认配置保存
local todayTimeStamp = tonumber(ARGV[6]); --今天的时间戳，如果redis的时间戳小于该值，说明刷新是昨天，用量需要清零
local json;
-- 判断用户角色，管理员则不计数
if (adminFlag == "true") then
    return 0; --success
else
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
        end
    else
        --不存在
        --local tmp = "{\"dirRefreshNumber\":{\"limit\":100,\"used\":0},\"lastModify\":1598577465461,\"urlPreloadNumber\":{\"limit\":500,\"used\":0},\"urlRefreshNumber\":{\"limit\":1000,\"used\":0}}";
        json = cjson.decode(contentJson);
        json['lastModify'] = timestamp;
    end
    local totalNum = 0;
    if("url" == type ) then
        totalNum = json['urlRefreshNumber']['used']  + size;
        if(totalNum > json['urlRefreshNumber']['limit']) then
            return -100; --超限
        else
            json['urlRefreshNumber']['used'] = totalNum;
        end
    elseif("dir" == type) then
        totalNum =  json['dirRefreshNumber']['used'] + size;
        if(totalNum > json['dirRefreshNumber']['limit']) then
            return -100;
        else
            json['dirRefreshNumber']['used'] = totalNum;
        end
    else
        totalNum =  json['urlPreloadNumber']['used'] + size;
        if(totalNum >  json['urlPreloadNumber']['limit']) then
            return -100;
        else
            json['urlPreloadNumber']['used'] = totalNum;
        end
    end
    local string = cjson.encode(json);
    redis.call('set',contentKey,string);
    return 0;
end