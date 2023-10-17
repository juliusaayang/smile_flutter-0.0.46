#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint smile_flutter.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'smile_flutter'
  s.version          = '0.0.46'
  s.summary          = 'A new flutter plugin project.'
  s.description      = <<-DESC
A new flutter plugin project.
                       DESC
  s.homepage         = 'http://docs.smileidentity.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Smile Identity' => 'support@smileidentity.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '12.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'armv7,i386,arm64', }
  s.swift_version = '5.0'

  s.static_framework = true
  s.dependency "Smile_Identity_SDK","2.1.38"
end
