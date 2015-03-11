/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.bookkeeper.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import org.apache.bookkeeper.client.BKException.BKNotEnoughBookiesException;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.net.DNSToSwitchMapping;
import org.apache.bookkeeper.stats.AlertStatsLogger;
import org.apache.bookkeeper.stats.StatsLogger;
import org.jboss.netty.util.HashedWheelTimer;

/**
 * Encapsulation of the algorithm that selects a number of bookies from the cluster as an ensemble for storing
 * data, based on the data input as well as the node properties.
 */
public interface EnsemblePlacementPolicy {

    /**
     * Initialize the policy.
     *
     * @param conf client configuration
     * @param optionalDnsResolver dns resolver
     * @param hashedWheelTimer timer
     * @param statsLogger stats logger
     * @param alertStatsLogger stats logger for alerts
     */
    public EnsemblePlacementPolicy initialize(ClientConfiguration conf,
                                              Optional<DNSToSwitchMapping> optionalDnsResolver,
                                              HashedWheelTimer hashedWheelTimer,
                                              StatsLogger statsLogger,
                                              AlertStatsLogger alertStatsLogger);

    /**
     * Uninitialize the policy
     */
    public void uninitalize();

    /**
     * A consistent view of the cluster (what bookies are available as writable, what bookies are available as
     * readonly) is updated when any changes happen in the cluster.
     *
     * @param writableBookies
     *          All the bookies in the cluster available for write/read.
     * @param readOnlyBookies
     *          All the bookies in the cluster available for readonly.
     * @return the dead bookies during this cluster change.
     */
    public Set<InetSocketAddress> onClusterChanged(Set<InetSocketAddress> writableBookies,
            Set<InetSocketAddress> readOnlyBookies);

    /**
     * Choose <i>numBookies</i> bookies for ensemble. If the count is more than the number of available
     * nodes, {@link BKNotEnoughBookiesException} is thrown.
     *
     * @param ensembleSize
     *          Ensemble Size
     * @param writeQuorumSize
     *          Write Quorum Size
     * @param excludeBookies
     *          Bookies that should not be considered as targets.
     * @return list of bookies chosen as targets.
     * @throws BKNotEnoughBookiesException if not enough bookies available.
     */
    public ArrayList<InetSocketAddress> newEnsemble(int ensembleSize, int writeQuorumSize, int ackQuorumSize,
            Set<InetSocketAddress> excludeBookies) throws BKNotEnoughBookiesException;

    /**
     * Choose a new bookie to replace <i>bookieToReplace</i>. If no bookie available in the cluster,
     * {@link BKNotEnoughBookiesException} is thrown.
     *
     * @param bookieToReplace
     *          bookie to replace
     * @param excludeBookies
     *          bookies that should not be considered as candidate.
     * @return the bookie chosen as target.
     * @throws BKNotEnoughBookiesException
     */
    public InetSocketAddress replaceBookie(int ensembleSize, int writeQuorumSize, int ackQuorumSize, Collection<InetSocketAddress> currentEnsemble, InetSocketAddress bookieToReplace,
                                           Set<InetSocketAddress> excludeBookies) throws BKNotEnoughBookiesException;

    /**
     * Reorder the read sequence of a given write quorum <i>writeSet</i>.
     *
     *
     *
     *
     * @param ensemble
     *          Ensemble to read entries.
     * @param writeSet
     *          Write quorum to read entries.
     * @param bookieFailureHistory
     *          Observed failures on the bookies
     * @return read sequence of bookies
     */
    public List<Integer> reorderReadSequence(ArrayList<InetSocketAddress> ensemble,
                                             List<Integer> writeSet, Map<InetSocketAddress, Long> bookieFailureHistory);


    /**
     * Reorder the read last add confirmed sequence of a given write quorum <i>writeSet</i>.
     *
     *
     *
     *
     * @param ensemble
     *          Ensemble to read entries.
     * @param writeSet
     *          Write quorum to read entries.
     * @param bookieFailureHistory
     *          Observed failures on the bookies
     * @return read sequence of bookies
     */
    public List<Integer> reorderReadLACSequence(ArrayList<InetSocketAddress> ensemble,
                                                List<Integer> writeSet, Map<InetSocketAddress, Long> bookieFailureHistory);
}
