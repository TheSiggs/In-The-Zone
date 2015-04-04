#!/bin/sh
tag=`git tag | head -n 1`
if [[ "`uname`" == "Darwin" ]]
then
  switch='E'
else
  switch='r'
fi
#git log --pretty=format:"- %s%n%b"|sed -$switch 's/^([^-].*)/  \1/g' > changelog.md
git log --pretty=format:"- %s%n%b" --since="$(git show -s --format=%ad `git rev-list --tags --max-count=1`)" | sed -$switch 's/^([^-].*)/  \1/g' > changelog-$tag.md
