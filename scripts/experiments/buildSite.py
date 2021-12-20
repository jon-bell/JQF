#!/usr/bin/python3
import sys
import os
import datetime
import shutil
import frontmatter
import subprocess
import json

benchmarks = "maven,closure,ant,rhino,bcel"

thisRev = sys.argv[1]  # "HEAD"
thisRevGHURL = sys.argv[2]  # "https://github.com/blasdf"
thisRevResultsDir = sys.argv[
    3
]  # "/ci-logs/public/jon-bell/JQF/eb6dcac50d6beae5ab2b7fd069bd72ef4bd7405e/evaluate/1581001040"


baseTemplateDir = sys.argv[4]  # "/experiment/jon/jqf-site/site-template"

baseURL = sys.argv[
    5
]  # "https://ci.in.ripley.cloud/logs/public/jon-bell/JQF/eb6dcac50d6beae5ab2b7fd069bd72ef4bd7405e/evaluate/1581001040/site/"

tempSiteDir = "./site_build"

dataDirs = {"dataDirs": [{"path": thisRevResultsDir + "/artifacts", "name": thisRev}]}

i = 6
print(len(sys.argv))
for j in range(6,len(sys.argv)):
    if i >=len(sys.argv):
        break
    dataDirs['dataDirs'].append({"name": sys.argv[i], "path": sys.argv[i+1]})
    i += 2

dataDirs = json.dumps(dataDirs).replace("\n","")
print(
    "Building site: "
    + thisRev
    + " "
    + thisRevGHURL
    + " "
    + thisRevResultsDir
    + " "
    + baseTemplateDir
    + " "
    + baseURL
    + " "
    + dataDirs
)

# Copy the site
if not os.path.isdir(tempSiteDir):
    shutil.copytree(baseTemplateDir, tempSiteDir)

templateFile = tempSiteDir + "/content/posts/template/template.Rmd"

# For each of the benchmarks, copy the benchmark tempalte page, overwite the vars
for bm in benchmarks.split(","):
    print(bm)
    os.mkdir(tempSiteDir + "/content/posts/" + bm)
    page = tempSiteDir + "/content/posts/" + bm + "/" + bm + ".Rmd"
    shutil.copy(templateFile, page)
    fp = open(page, "r")
    rmd = fp.read();
    rmd = rmd.replace("$BENCHMARK", bm).replace("$DATADIRS",dataDirs)
    fp.close()
    fp = open(page, "wt")
    fp.write(rmd)
    fp.close()

# Delete the template
shutil.rmtree(tempSiteDir + "/content/posts/template")

# Set up the site config
configFile = tempSiteDir + "/config.yaml"

fp = open(configFile, "r")
config = fp.read()
fp.close()
config = (
    config.replace("$REVGH", thisRev)
    .replace("$GHLINK", thisRevGHURL)
    .replace("baseurl: /", "baseurl: " + baseURL)
    .replace("$DURATION", os.environ.get("DURATION"))
    .replace("$TRIALS", os.environ.get("TRIALS"))
    .replace("$REPORTDATE", datetime.datetime.now().strftime("%m/%d/%Y %H:%M %Z"))
)
fp = open(configFile, "wt")
fp.write(config)
fp.close()

# Build the site
newSiteDir = thisRevResultsDir + "/site"
os.chdir(tempSiteDir)
subprocess.call('R -e "renv::restore()"', shell=True)
subprocess.call("R -e \"library('blogdown')\"", shell=True)
subprocess.call("R -e \"blogdown::install_hugo('0.90.1')\"", shell=True)
subprocess.call("R -e \"blogdown::build_site(build_rmd = 'newfile')\"", shell=True)
shutil.copytree("public", newSiteDir)

print("Deployed website to: " + baseURL)
