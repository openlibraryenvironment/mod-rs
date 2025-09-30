A quickstart summary to installing [k3d](https://k3d.io) using the
[asdf](https://asdf-vm.com/) version manager.

Checkout asdf into `~/.asdf`:
```
git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.14.0
```

At the end of .bashrc add:
```
. "$HOME/.asdf/asdf.sh"
. "$HOME/.asdf/completions/asdf.bash"
```

Then you can install k3d:

```
asdf plugin-add k3d
asdf install k3d latest
asdf global k3d latest
```

You might also consider k9s, sort of like top for all the things kubectl shows:

```
asdf plugin-add k9s
asdf install k9s latest
asdf global k9s latest
```
