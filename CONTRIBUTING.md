# Contributing to Taikai

First off, thank you for taking the time to contribute! :+1: :tada:

### How to Contribute

#### Ask questions

If you believe there is an issue, search through existing issues for this project trying a
few different ways to find discussions, past or current, that are related to the issue.
Reading those discussions helps you to learn about the issue, and helps us to make a
decision.

#### Create an Issue

Reporting an issue or making a feature request is a great way to contribute. Your feedback
and the conversations that result from it provide a continuous flow of ideas. However,
before creating a ticket, please take the time to [ask and research](#ask-questions) first.

Once you're ready, create an issue on the module.

#### Before submitting a Pull Request

To contribute to this project, please fork the repository and submit a pull request with your changes. Please ensure that your changes adhere to the following guidelines:

* Code should follow the conventions.
* All code should be well-documented.
* All new functionality should be covered by tests.
* Changes should not break existing functionality.

#### Submit a Pull Request

1. Always check out the `main` branch and submit pull requests against it.

2. Choose the granularity of your commits consciously and squash commits that represent
   multiple edits or corrections of the same logical change. See
   [Rewriting History section of Pro Git](https://git-scm.com/book/en/Git-Tools-Rewriting-History)
   for an overview of streamlining the commit history.

3. Format commit messages using 55 characters for the subject line, 72 characters per line
   for the description, followed by the issue fixed, e.g. `Closes gh-12279`. See the
   [Commit Guidelines section of Pro Git](https://git-scm.com/book/en/Distributed-Git-Contributing-to-a-Project#Commit-Guidelines)
   for best practices around commit messages, and use `git log` to see some examples.

4. If there is a prior issue, reference the GitHub issue number in the description of the
   pull request.


#### Participate in Reviews

Helping to review pull requests is another great way to contribute. Your feedback
can help to shape the implementation of new features. When reviewing pull requests,
however, please refrain from approving or rejecting a PR unless you are a core
committer for this project.

### Code Conventions

#### Java
Please follow the Google Java Style Guide when writing Java code for this project. You can also import the Intellij IDEA code style configuration file for Java from [here](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml).
