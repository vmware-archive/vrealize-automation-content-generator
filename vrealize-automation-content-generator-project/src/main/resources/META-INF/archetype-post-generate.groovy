/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

dir = new File(new File(request.outputDirectory), request.artifactId)
def run(String cmd) {
    def process = cmd.execute(null, dir)
    process.waitForProcessOutput((Appendable)System.out, System.err)
    if (process.exitValue() != 0) {
        throw new Exception("Command '$cmd' exited with code: ${process.exitValue()}")
    }
}

def mvnCommand = System.properties['os.name'].toLowerCase().contains('windows') ? 'mvn.cmd' : 'mvn'
if(System.properties['initialize'] == "true") {
    run("${mvnCommand} deploy -PreverseGenerate")
}