# Handling Translations

<secondary-label ref="wip" />

Normally, devs must handle formatting themselves. While ByteLib handles language files automatically, if developers want formatting capabilities, this is a manual process.

ByteLib contains a `MiniMessageTranslator` that can be used to help with this process. Use of these methods requires injecting `Translator` from ByteLib.

## Lang > Component translation
Call the `tr()` method to automatically handle MiniMessage tags in your lang files. This method takes a key, which is the key of the message in your language file, as well as a list of optional `TagResolver`s. `TagResolver` is an Adventure API type, please reference Adventure API's documentation for info on this.

`tr()` will automatically return the proper message from the currently active language file, handling translating colors and other tags. If your language file contains a `prefix` value, this will be treated as a TagResolver. Any instance of `<prefix>` in your messages will be translated to the value of `prefix` in your language file!

## Lang > Title translations
A utility method, which is primarily meant for my plugins, is `title()`, which allows you to easily craft titles from your language file. A title is **required** to be formatted as such:
```yaml
# lang/[langFile].yml
titleKey:
  title: Title value
  subtitle: Subtitle value
```

`TagResolver`s can be passed into this method, just like `tr`, and the times can be configured as well.