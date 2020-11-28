local key = KEYS[1]

local currentTime = tonumber(ARGV[1])
local expire = tonumber(ARGV[2])

local lastTime = tonumber(redis.call('get', key) or "0")

if lastTime == -1 or currentTime == -1 then
    redis.call("set", key, currentTime)
    return -200
end

if currentTime - lastTime > expire then
    redis.call("set", key, currentTime)
    return 0
else
    return -100
end