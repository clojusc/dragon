kibit:
	@echo
	@echo "#######################"
	@echo "#  kibit Lint Checks  #"
	@echo "#######################"
	@lein with-profile +test kibit && echo ok

bikeshed:
	@echo
	@echo "##########################"
	@echo "#  bikeshed Lint Checks  #"
	@echo "##########################"
	@lein with-profile +test bikeshed

base-eastwood:
	@echo
	@echo "##########################"
	@echo "#  eastwood Lint Checks  #"
	@echo "##########################"
	@lein with-profile +test eastwood "$(EW_OPTS)"

yagni:
	@echo
	@echo "#######################"
	@echo "#  yagni Lint Checks  #"
	@echo "#######################"
	@lein with-profile +test yagni

eastwood:
	@EW_OPTS="{:namespaces [:source-paths]}" make base-eastwood

lint: kibit eastwood bikeshed

lint-unused:
	@EW_OPTS="{:linters [:unused-fn-args :unused-locals :unused-private-vars] :namespaces [:source-paths]}" make base-eastwood

lint-ns:
	@EW_OPTS="{:linters [:unused-namespaces :wrong-ns-form] :namespaces [:source-paths]}" make base-eastwood

check: lint
	@lein test

ancient:
	@lein with-profile +test ancient check.check-profiles
