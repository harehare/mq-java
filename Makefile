.PHONY: setup build-rust build test clean

MQ_REPO_URL := https://github.com/harehare/mq
MQ_REPO_DIR := .mq
MQ_FFI_DIR  := $(MQ_REPO_DIR)/crates/mq-ffi
MQ_LIB_DIR  := $(MQ_REPO_DIR)/target/release

setup:
	@if [ -d "$(MQ_REPO_DIR)/.git" ]; then \
		git -C $(MQ_REPO_DIR) pull --ff-only -q; \
	else \
		git clone --depth=1 $(MQ_REPO_URL) $(MQ_REPO_DIR); \
	fi

build-rust: setup
	cd $(MQ_REPO_DIR) && cargo build --release -p mq-ffi

build: build-rust
	mvn compile -q

test: build-rust
	mvn test -Djna.library.path=$(MQ_LIB_DIR)

clean:
	mvn clean -q
