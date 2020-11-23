local key = KEYS[1]

local newtid = ARGV[1]

local currentTid = redis.call('get', key)

if(currentTid) then
    return currentTid;
else
    redis.call("set", key, newtid)
    redis.call("EXPIRE", key, 1)
    return newtid
end