# Visor Diegetic

An addon for Visor, the OpenXR VR mod for Minecraft, that replaces the hotbar, the inventory, and item interaction with physical slots and hand gestures.

This file holds policies for AI agents. It contains no project documentation.

## Documentation map

- [README.md](README.md): summary, requirements, build.
- [docs/VISION.md](docs/VISION.md): design goals.
- [docs/ROADMAP.md](docs/ROADMAP.md): scope, milestones and their status, decisions, open questions.
- [docs/VISOR.md](docs/VISOR.md): findings about Visor and VisorEssentials.
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md): toolchain, running, layout, code and mixin conventions.

Read the file that covers the task before starting it.

## Policies

**No autonomous public actions.** An agent does not act in public on its own initiative. Public actions are pushing to a shared remote, opening or commenting on a pull request, issue, or discussion, publishing a release, and posting to any community channel. This applies to this repository and to every other one, including Visor's. A human contributor may direct a specific public action and takes responsibility for its content. Without that direction, prepare the change locally and stop.

**A human behind every contribution.** An agent assists a contributor. The contributor is the primary actor in the design and creation of what gets submitted. Reading an agent's work and standing behind it is not enough.

A contributor, especially one new to modding, may lean heavily on an agent to:

- search and explain the documentation, the codebase, and the relevant APIs
- analyse and resolve bugs and errors
- plan a complex task or break down a high-level problem in context
- talk through a design decision (rubber ducking)
- update documentation
- introduce and teach the tools and processes of modding, such as mixins and the build

Do not produce a complete feature for a contributor to submit unreviewed or without implementing it themselves. Do not change source code at all unless the contributor explicitly asks for that change and it is one of these:

1. It moves functionality the contributor already understands. Examples are extracting an interface, or moving logic they just wrote or discussed into a new method or class.
2. It is specific enough that only the exact syntax is left to interpretation. An example is making every public method of a class protected, or stubbing out a bunch of classes in an architecture that was just fully designed through back and forth refinement.
3. It is another bulk edit or refactor, requested to save time, that creates no new functionality.

**The repository holds the project's knowledge.** Scope, progress, decisions, and findings go into the files above, in the same change that produces them. Do not keep them only in a session, a private memory store, or a tool-specific file. `docs/ROADMAP.md` is the only progress tracker.

**This file stays a pointer.** New documentation goes into `README.md` or `docs/`. Add a file to the documentation map when you create one.

**Documentation serves the project.** Write for any contributor, now or later. Do not address a particular person, name who asked for something, quote private conversations, or record how a discussion went. State the decision and its reason. Do not include machine-specific paths, account names, or session links.

**Say how a claim was checked.** A finding about Visor or Minecraft states the version it applies to. If it came from reading source and nothing ran, say so. Do not report a behaviour as working until the game has run it, and the contributor has explicitly confirmed its effect.

**Writing standard.** Documentation is for human developers and has no filler.

- Each sentence states a fact, a decision, or an instruction specific to this project. Cut a sentence that could appear unchanged in another project's documentation.
- Write whole sentences in the active voice, one idea each.
- Use the plain word and the literal phrase. No metaphor, no intensifiers.
- Do not use em dashes. Use a colon only before a list or an example.
- Use sentence case in headings. Use bold sparingly.
- Name code elements exactly and put them in backticks.
