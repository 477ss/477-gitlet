# 477-gitlet

简化版git，实现了add commit reset等本地功能和[伪]远程功能push pull fetch


HEAD：存放当前所在分支指针

objects：项目所管理的所有对象，分为blob和commit两类以及相应索引

refs：分支，包括本地分支和远程分支（设计时这里真的是坑。。如果打算写远程功能的话一定要注意）

remote_repo：存放远程仓库的信息（别名，地址）

stage：暂存区。包括待添加和待删除文件。


其他信息：

https://www.cnblogs.com/ub477/articles/17509175.html


