#####测试环节：
1. 启动8080，查看相关日志，是否按时发送vote给其他node。有无其他错误。
当超时时是否再次发送vote

-- ok，日志顺序正确，没有明显错误，每次都新生成【选举超时】，这是关键，
能防止大家都同一时间发送，然后等待同一时间，然后再同时发送。


当收到vote时查看信息是否正确，是否超过半数则成为leader，
当成为leader时，是否发送心跳，是否不再发送vote。查看是否一直持续
发送heartbeat，然后不发送vote。



启动8081，查看相关日志，查看是否等待投票，查看是否收到vote，查看是否
收到heartbeat。查看heartbeat频率以及是否发送vote。

启动8082，查看相关日志，查看是否等待投票，查看是否收到vote，查看是否
收到heartbeat。查看heartbeat频率以及是否发送vote。

停止8080，查看8081是否不在收到heartbeat，是否准备投票，发起投票后，
是否收到vote，是否成为leader，进而是否发送heartbeat，是否持续。
查看8082是否不在收到heartbeat，是否准备投票，发起投票后，
是否收到vote，是否成为leader，进而是否发送heartbeat，是否持续。

暂停8080，查看8081是否不在收到heartbeat，是否准备投票，发起投票后，
是否收到vote，是否成为leader，进而是否发送heartbeat，是否持续。
查看8082是否不在收到heartbeat，是否准备投票，发起投票后，
是否收到vote，是否成为leader，进而是否发送heartbeat，是否持续。
然后继续8080，查看term相关是否导致他的heartbeat被拒绝，然后他是否
能成为follower，然后查看8081和8082的反应。

暂停非leader机器，查看leader和其他follower的反应。
再恢复leader，查看所有机器的日志。


查看genElectionTime生成次数是否过多。