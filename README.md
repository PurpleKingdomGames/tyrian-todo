# Tyrian • [TodoMVC](http://todomvc.com)

> Tyrian is an Elm-inspired, purely functional web framework for Scala 3. Its purpose is to make building interactive websites in Scala 3 fun! Tyrian allows you to describe web pages and complex interactions in a way that is elegant, easy to read and easy to reason about.

This is an implementation of the standard TodoMVC project using Tyrian, which acts as a rosetta stone for most common JavaScript frameworks.

You can see if running [here](https://github.com/PurpleKingdomGames/tyrian-todo).

## Resources

- [Website](https://tyrian.indigoengine.io/)
- [Documentation](https://tyrian.indigoengine.io/concepts/guided-tour/)

### Support

- [Twitter](https://twitter.com/indigoengine)
- [Discord](https://discord.gg/b5CD47g)

*Let us [know](https://github.com/PurpleKingdomGames/tyrian/issues) if you discover anything worth sharing.*

## Credit

Created by [Dave Smith](https://github.com/davesmith00000)

---

## Running Locally

To run the program in a browser you will need to have yarn (or npm) installed.

Before your first run and for your tests to work, **you must** install the node dependencies with:

```sh
yarn install
```

This example uses Parcel.js as our bundler and dev server, there are lots of other options you might prefer like Webpack, scalajs-bunder, or even just vanilla JavaScript.

We recommend you have two terminal tabs open in the directory containing this README file.

In the first, we'll run sbt.

```sh
sbt
```

From now on, we can recompile the app with `fastOptJS` or `fullOptJS` _**but please note** that the `tyrianapp.js` file in the root is expecting the output from `fastOptJS`_.

Run `fastOptJS` now to get an initial build in place.

Then start your dev server, with:

```sh
yarn start
```

Now navigate to [http://localhost:1234/](http://localhost:1234/) to see your site running.

If you leave parcel's dev server running, all you have to do is another `fastOptJS` or `fullOptJS` and your app running in the browser should hot-reload the new code.
