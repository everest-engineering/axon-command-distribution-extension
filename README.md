# Distributed command dispatching for Axon
[![Build Status](https://travis-ci.com/everest-engineering/axon-command-distribution-extension.svg?branch=master)](https://travis-ci.com/everest-engineering/axon-command-distribution-extension)

This is a supporting repository for [Lhotse](https://github.com/everest-engineering/lhotse), a starter kit for writing event sourced web applications following domain driven design principles.

Part of Axon's appeal is the ability to horizontally scale an application using Axon Server to for command and event dispatching, and event log persistence. While well suited to applications that require massive scale, deploying Axon Server introduces additional maintenance and configuration overhead.

We have taken a different approach that provides a good starting point for small to medium applications while still allowing migration to Axon Server in the future. Horizontal scalability is achieved via a command distribution extension that wraps the standard Axon command gateway with a [Hazelcast](https://hazelcast.com/) based distributed command gateway. An arbitrary number of application instances started together, either on the same network or within a Kubernetes cluster, will be automatically discovered to form a cluster.

Commands dispatched through the Hazelcast command gateway are deterministically routed to a single application instance which, based on the aggregate identifier, has ownership of an aggregate for as long as that instance remains a member of the cluster. This clear aggregate ownership is vital to avoid a split brain scenario as aggregates are cached in memory and are responsible for command validation. A split brain situation could arise if multiple unsynchronised copies were to be distributed among cluster members.

Hazelcast will automatically reassign aggregate ownership if an application instance leaves the cluster due to a restart, network disconnection or other failure.

Events emitted by an aggregate are passed to the application instance's local event bus. Events are persisted to the event log by the instance handling the command. Subscribing event processing (the default in our configuration) guarantee that the same instance will be performing the event handling.
