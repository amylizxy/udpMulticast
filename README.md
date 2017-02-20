# 实现局域网内 Android 设备相互发现
用于局域网中，在不知道设备的内网IP的情况下，通过UDP组播搜索局域网内所有的的设备

# udp组播原理，请见我的博客 http://blog.csdn.net/lixin88/article/details/55209630

# 对应的代码实现说明，请见 http://blog.csdn.net/lixin88/article/details/56013014

# 使用说明
  app 只需要集成LANDiscoveryLib 这个library工程，具体调用，如果是搜索者请参考 phoneClient中的调用，如果是被搜索者请参考tvServer中的调用；
  client 包下，对应搜索者；server 包下，对应被搜索者；
  base 包下 均为封装的一些交互数据和udp包格式等；
