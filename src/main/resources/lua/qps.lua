local key = KEYS[1]
-- 限流大小
local limit = tonumber(ARGV[1])

-- 获取当前流量大小
local curentLimit = tonumber(redis.call('get', key) or "0")

if curentLimit + 1 > limit then
    -- 达到限流大小 返回
    return -100;
else
    -- 没有达到阈值 value + 1
    redis.call("INCRBY", key, 1)
    redis.call("EXPIRE", key, 1)
    return 0
end