Versioning
==========

The version is managed like this:

Semantic version system. See http://semver.org/ .

So, the version is encoded as
x.y.z
where 
x is major
y is minor
z is revision

Revision is upgraded with every release. For us this means
every week when we package an installer.

Tags are of the scheme
vx.y.z
and need to be manually updated with

```git tag -a v0.1.2```

for example.

That has to be done after a 
```git pull```

```git push --tags```
pushes the tags, although a simple
```git push```
may be sufficient.
