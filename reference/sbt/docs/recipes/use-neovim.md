Use Neovim
==========

  [metals]: https://scalameta.org/metals/
  [nvim-metals]: https://github.com/scalameta/nvim-metals
  [lsp.lua]: https://github.com/scalameta/nvim-metals/discussions/39#discussion-82302

```admonish warning
This is a draft documentation of sbt 2.x that is yet to be released.
This is a placeholder, copied from sbt 1.x.
```

Objective
---------

I want to use Metals on Neovim with sbt as the build server.

Steps
-----

Chris Kipp, who is a maintainer of Metals, created [nvim-metals][nvim-metals] plugin that provides comprehensive Metals support on Neovim. To install nvim-metals, create `lsp.lua` under `$XDG_CONFIG_HOME/nvim/lua/` based on Chris's [lsp.lua][lsp.lua] and adjust to your preference. For example, comment out its plugins section and load the listed plugins using the plugin manager of your choice such as vim-plug.

In `init.vim`, the file can be loaded as:

```lua
lua << END
require('lsp')
END
```

Per `lsp.lua`, `g:metals_status` should be displayed on the status line, which can be done using lualine.nvim etc.

1. Next, open a Scala file in an sbt build using Neovim.
2. Run `:MetalsInstall` when prompted.
3. Run `:MetalsStartServer`.
4. If the status line is set up, you should see something like "Connecting to sbt" or "Indexing."<br>
   <img src="../files/nvim0.png" width="900">
5. Code completion works when you're in Insert mode, and you can tab through the candidates:<br>
   <img src="../files/nvim1.png" width="900">

- A build is triggered upon saving changes, and compilation errors are displayed inline:<br>
  <img src="../files/nvim2.png" width="900">

#### Go to definition

1. You can jump to definition of the symbol under cursor by using `gD` (exact keybinding can be customized):<br>
   <img src="../files/nvim3.png" width="900">
2. Use `Ctrl-O` to return to the old buffer.

#### Hover

- To display the type information of the symbol under cursor, like hovering, use `K` in Normal mode:<br>
   <img src="../files/nvim4.png" width="900">

#### Listing diagnostics

1. To list all compilation errors and warnings, use `<leader>aa`:<br>
   <img src="../files/nvim5.png" width="900">
2. Since this is in the standard quickfix list, you can use the command such as `:cnext` and `:cprev` to nagivate through the errors and warnings.
3. To list just the errors, use `<leader>ae`.

#### Interactive debugging with Neovim

1. Thanks to nvim-dap, Neovim supports interactive debugging. Set break points in the code using `<leader>dt`:<br>
   <img src="../files/nvim6.png" width="900">
2. Nagivate to a unit test, confirm that it's built by hovering (`K`), and then
   "debug continue" (`<leader>dc`) to start a debugger.
   Choose "1: RunOrTest" when prompted.
3. When the test hits a break point, you can inspect the values of the variables by debug hovering (`<leader>dK`):<br>
   <img src="../files/nvim7.png" width="900">
4. "debug continue" (`<leader>dc`) again to end the session.

See [nvim-metals][nvim-metals] regarding further details.

#### Logging into sbt session

We can also log into the existing sbt session using the thin client.

1. In a new vim window type `:terminal` to start the built-in terminal.
2. Type in `sbt --client`<br>
   <img src="../files/nvim8.png" width="900">

Even though it's inside Neovim, tab completion etc works fine inside.
