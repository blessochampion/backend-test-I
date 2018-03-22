## Back-end Developer Test

### Devcenter Backend Developer Test I

The purpose of this test is not only to quickly gauge an applicant's abilities with writing codes, but also their approach to development.

Applicants may use whatever language they want to achieve the outcome.

## Task

Build a bot that extracts the following from people’s Twitter bio (on public/open accounts), into a Google spreadsheet:

* Twitter profile name 
* Number of followers

Target accounts using either of these criteria:
* Based on hashtags used
* Based on number of followers; Between 1,000 - 50,000

The bot is suppose to maintain a session and continously listen to the predefined hashtag

## How to complete the task

1. Fork this repository into your own public repo.

2. Complete the project and commit your work. Make a screencast of how it works with the googlespread sheet and progam side-by-side

3. Send the URL of your own repository and the screencast to @kolawole.balogun on the Slack here bit.ly/dcs-slack.

## Show your working

If you choose to use build tools to compile your CSS and Javascript (such as SASS of Coffescript) please include the original files as well. You may update this README file outlining the details of what tools you have used.

## Clean code

This fictitious project is part of a larger plan to reuse templates for multiple properties. When authoring your CSS ensure that it is easy for another developer to find and change things such as fonts and colours.


## Good luck!

We look forward to seeing what you can do. Remember, although it is a test, there are no specific right or wrong answers that we are looking for - just do the job as best you can. Any questions - create an issue in the panel on the right (requires a Github account).


## Demo
![screen shot](https://user-images.githubusercontent.com/8668661/33088863-330b4250-ceef-11e7-9e9c-b4fd9ca299d8.gif)








## My Approach
Two tools were used in the implementation
1. [Twitter4j](http://twitter4j.org/en/)

  This is an unofficial library for twitter api. It was used for connecting to the twitter streaming api.
  To use the twitter api, you need to create a twitter app [@apps.twitter.com](https://apps.twitter.com). You will need the following     credentials for successfull connection.
  ```
  Consumer Key (API Key)
  Consumer Secret (API Secret)
  ```
  Generate access token, to get:
  ```
  Access Token
  Access Token Secret
  ```
 
 2. [GoogleSheets Api](https://developers.google.com/sheets/api/)
 
 Google sheets api allows you to read/write to your google sheets documents. Check https://developers.google.com/sheets/api/quickstart/java on how to setup a project.
 Don't forget to add `client_secret.json` to your working directory.... so you have `src/main/resources/client_secret.json`
 
## Running the Project

 A *property file* that contains the credentials for both Twitter and Google Sheet is required to start the app.
 The property file should should contains the following:
 
   ```
  consumer_key=
  consumer_secret=
  access_token=
  access_token_secret=
  sheet_id=
  column_name=
  column_followers_count=
  queue_count=
  ```

The `consumer_key`, `consumer_secret`, `access_token`, `access_token_secret`  are the ones gotten from twitter.
`sheet_id` is the id of the google sheet document. For example https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit#gid=0
the `sheet_id` = 1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms
`column_name` is the column header name for the google sheet column that will contain the twiter username.
`column_followers_count` is the header name for the google sheet column that will contain the twitter follower count.
*Note:* that writing to Google Sheet requires api request, if you will like the Bot to write to the sheet after say 5 user data is available, `queue_count=5`. `queue_count` can take values between 1 and 10 inclusive.

To run the app:

    java App propertyFile


Where `propertyFile` is the file that contains the keyValue pair of your credentials.

## Demo
You can checkout the video Demo [HERE](https://www.dropbox.com/s/8ksb5kayr8r8f7r/twitterbot.mp4?dl=0)

