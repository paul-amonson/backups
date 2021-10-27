// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package backups

import spock.lang.Specification

class BackupTest extends Specification {
    def "application has a greeting"() {
        setup:
        def app = new Backup()

        when:
        def result = app.greeting

        then:
        result != null
    }
}
