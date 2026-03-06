# ByteLib Loader

<primary-label ref="subject-to-change" />

The ByteLib <tooltip term="loader">loader</tooltip> is responsible for loading ByteLib's dependencies for plugins using
it to operate. You should've configured your plugin to use it in the [previous topic](Bootstrapping.md).

Currently, the loader is _not configurable_, but it is also _not mandatory_ to use it. The loader is defined as follows:

<include from="code_snippets.topic" element-id="bytelib-loader" />

If you need or want to use your own loader, as long as it includes those two dependencies, then you do not have to use the
included one. The loader is only called by Paper, ByteLib does not do anything with it itself.