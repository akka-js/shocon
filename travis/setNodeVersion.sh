#!/bin/sh

if [ -n "$TRAVIS_NODE_VERSION" ]; then
  rm -rf ~/.nvm
  git clone https://github.com/creationix/nvm.git ~/.nvm
  (cd ~/.nvm && git checkout "$(git describe --abbrev=0 --tags)")
  source ~/.nvm/nvm.sh
  nvm install "$TRAVIS_NODE_VERSION"
  npm install
fi;
