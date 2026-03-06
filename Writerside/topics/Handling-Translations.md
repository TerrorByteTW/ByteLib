# Handling Translations

<secondary-label ref="wip" />

Normally, devs must handle formatting themselves. While ByteLib handles *loading* language files automatically,
if developers want formatting capabilities, this is a manual process. As well, the underlying BoostedYAML library
only returns strings, and most Paper methods that use Strings are deprecated (such as `Player#sendMessage()`).
Components are first class in Paper, so ByteLib should treat them as such, too.

ByteLib has a MiniMessage Translator that can be used to help with this process. Use of these methods requires
injecting `Translator` from ByteLib.

> Use of `Translator` requires using `ByteLibConfig`

## Getting started

Perhaps one of the easiest features to start using, inject `Translator` into classes that need it:

```java
public class SomeClass {
    private final Translator translator;

    @Inject
    public SomeClass(Translator translator) {
        this.translator = translator;
    }
}
```

`Translator` is a tiny utility, only containing two methods: `tr()` and `title()`.

## `tr()`

```java
public Component tr(String key, TagResolver... resolvers);
```

The `tr()` method automatically handles MiniMessage tags in your lang files. This method takes a key, which is
the key of the message in your language file, as well as a list of optional `TagResolver`s. `TagResolver` is an
Adventure API type, please reference Adventure API's documentation for info on this.

`tr()` will automatically return the proper message from the currently active language file, handling translating colors
and other tags.

## `title()`

```java
public Title title(String keyBase, Title.Times times, TagResolver... resolvers);
```

`title()` is a unique utility method that allows you to easily craft titles from your language file. Titles are
extremely easy to craft, but this method takes a *smidge* of boilerplate out of your plugins. A title is **required**
to be formatted as such:

```yaml
# lang/[langFile].yml
titleKey:
  title: Title value
  subtitle: Subtitle value
```

In the example above, `titleKey` would be passed into the `keyBase` parameter of `title()`. `Title.Times` is the
standard object that Paper uses to present Title timings.

`TagResolver`s can be passed into this method, just like `tr()`.

## Special Translation Keys

Many plugins have a "prefix" in their messages that indicates where a message came from. It also acts as "branding" for
your plugin in-game.

A built-in MiniMessage `TagResolver` exists which will translate `<prefix>` tags to a `prefix`
value in your language file, if included. This provides an easy way to prefix all translations with a configurable
prefix. If `prefix` does not exist in your language file, `<prefix>` will be translated to an empty string instead.

```yaml
language-version: 1
prefix: <blue>[My Prefix]</blue>
some-message: <prefix> This is a message!
some-other-message: <prefix> This will be prefixed, too!
```

{collapsible="true" collapsed-title="Prefix example"}